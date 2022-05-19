package blue.starry.setlist

import blue.starry.penicillin.core.request.action.CursorJsonObjectApiAction
import blue.starry.penicillin.endpoints.account
import blue.starry.penicillin.endpoints.account.verifyCredentials
import blue.starry.penicillin.endpoints.friends
import blue.starry.penicillin.endpoints.friends.listUsersByScreenName
import blue.starry.penicillin.endpoints.friends.listUsersByUserId
import blue.starry.penicillin.endpoints.lists
import blue.starry.penicillin.endpoints.lists.*
import blue.starry.penicillin.extensions.*
import blue.starry.penicillin.extensions.cursor.byCursor
import blue.starry.penicillin.extensions.cursor.nextCursor
import blue.starry.penicillin.models.User
import blue.starry.penicillin.models.cursor.CursorUsers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object Setlist {
    private const val listMemberLimit = 5000
    private val owner = runBlocking {
        SetlistTwitterClient.account.verifyCredentials.execute()
    }

    private val targetList by lazy {
        val (id, slug) = Env.TARGET_LIST_ID to Env.TARGET_LIST_SLUG

        runBlocking {
            when {
                id != null -> {
                    SetlistTwitterClient.lists.show(id).execute().result
                }
                slug != null -> {
                    SetlistTwitterClient.lists.showByOwnerId(slug = slug, ownerId = owner.result.id).execute().result
                }
                else -> {
                    val ownedLists = SetlistTwitterClient.lists.list.execute()
                    error("Both of TARGET_LIST_ID and TARGET_LIST_SLUG are not present. FYI, you currently own the following lists.\n${ownedLists.joinToString(", ") { "${it.name} (ID: ${it.id}, Slug: \"${it.slug}\")" }}")
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun merge() = coroutineScope {
        val previousUsers = SetlistTwitterClient.lists
            .members(
                listId = targetList.id,
                count = 5000
            )
            .asFlow()
            .toList()
        val previousUserIds = previousUsers.map { it.id }.toSet()
        val currentUsers = listOf(
            getMembersByListIds(),
            getMembersByListSlugs(),
            getFollowingUsersByUserIds(),
            getFollowingUsersByUserScreenNames(),
            if (Env.SOURCE_USER_INCLUDE_SELF) flowOf(owner.result) else emptyFlow()
        ).asFlow().flattenConcat().distinctUntilChangedBy { it.id }.toList()
        val currentUserIds = currentUsers.map { it.id }.toSet()

        val willBeAdded = currentUsers.filter { it.id !in previousUserIds }
        val willBeRemoved = previousUsers.filter { it.id !in currentUserIds }

        if (willBeAdded.isEmpty() && willBeRemoved.isEmpty()) {
            return@coroutineScope
        }

        if (previousUsers.size + willBeAdded.size - willBeRemoved.size > listMemberLimit) {
            logger.warn { "Target list exceeds member limit ($listMemberLimit)." }
            return@coroutineScope
        }

        willBeRemoved.chunked(100).mapNotNull { chunk ->
            if (Env.DRYRUN) {
                return@mapNotNull null
            }

            launch {
                SetlistTwitterClient.lists.removeMembersByUserIds(
                    listId = targetList.id,
                    userIds = chunk.map { it.id }
                ).execute()

                logger.debug { "Called destroy_all for ${chunk.size} users. [${chunk.joinToString(", ") { it.screenName }}]" }
            }
        }.joinAll()

        willBeAdded.chunked(100).mapNotNull { chunk ->
            if (Env.DRYRUN) {
                return@mapNotNull null
            }

            launch {
                SetlistTwitterClient.lists.addMembersByUserIds(
                    listId = targetList.id,
                    userIds = chunk.map { it.id }
                ).execute()

                logger.debug { "Called create_all for ${chunk.size} users. [${chunk.joinToString(", ") { it.screenName }}]" }
            }
        }.joinAll()

        logger.info {
            buildString {
                appendLine("These were some changes to target list. Members which were added to the list was marked with plus sign, and removed with minus sign.")

                if (willBeAdded.isNotEmpty()) {
                    appendLine(willBeAdded.sortedBy { it.name }
                        .joinToString("\n") { "[+] ${it.name} @${it.screenName} (ID: ${it.id})" })
                }

                if (willBeRemoved.isNotEmpty()) {
                    appendLine(willBeRemoved.sortedBy { it.name }
                        .joinToString("\n") { "[-] ${it.name} @${it.screenName} (ID: ${it.id})" })
                }

                append("Summary: ${willBeAdded.size} users were added, ${willBeRemoved.size} users were removed.")
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun getMembersByListIds(): Flow<User> {
        if (Env.SOURCE_LIST_IDS.isEmpty()) {
            return emptyFlow()
        }

        return Env.SOURCE_LIST_IDS.asFlow().flatMapConcat {
            SetlistTwitterClient.lists.members(
                listId = it,
                count = 5000
            ).asFlow()
        }
    }

    @OptIn(FlowPreview::class)
    private fun getMembersByListSlugs(): Flow<User> {
        if (Env.SOURCE_LIST_SLUGS.isEmpty()) {
            return emptyFlow()
        }

        return Env.SOURCE_LIST_SLUGS.asFlow().flatMapConcat {
            val (screenName, slug) = it.split("/")

            SetlistTwitterClient.lists.membersByOwnerScreenName(
                slug = slug,
                ownerScreenName = screenName,
                count = 5000
            ).asFlow()
        }
    }

    @OptIn(FlowPreview::class)
    private fun getFollowingUsersByUserIds(): Flow<User> {
        val ids = if (Env.SOURCE_USER_INCLUDE_SELF) {
            Env.SOURCE_USER_IDS.plus(owner.result.id)
        } else {
            Env.SOURCE_USER_IDS
        }

        if (ids.isEmpty()) {
            return emptyFlow()
        }

        return ids.asFlow().flatMapConcat {
            SetlistTwitterClient.friends.listUsersByUserId(
                userId = it,
                count = 200,
                skipStatus = true,
                includeUserEntities = false
            ).asFlow()
        }
    }

    @OptIn(FlowPreview::class)
    private fun getFollowingUsersByUserScreenNames(): Flow<User> {
        if (Env.SOURCE_USER_SCREEN_NAMES.isEmpty()) {
            return emptyFlow()
        }

        return Env.SOURCE_USER_SCREEN_NAMES.asFlow().flatMapConcat {
            SetlistTwitterClient.friends.listUsersByScreenName(
                screenName = it,
                count = 200,
                skipStatus = true,
                includeUserEntities = false
            ).asFlow()
        }
    }

    private fun CursorJsonObjectApiAction<CursorUsers, User>.asFlow(): Flow<User> = flow {
        val first = execute()
        emitAll(first.result.items.asFlow())

        var cursor = first.nextCursor
        while (cursor != 0L) {
            val response = first.byCursor(cursor).execute()
            emitAll(response.result.items.asFlow())

            cursor = response.nextCursor

            val rateLimit = response.rateLimit ?: continue
            if (rateLimit.isExceeded) {
                rateLimit.awaitRefresh()
            }
        }
    }
}

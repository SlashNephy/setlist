package blue.starry.setlist

import kotlin.properties.ReadOnlyProperty

object Env {
    val TWITTER_CK by string
    val TWITTER_CS by string
    val TWITTER_AT by string
    val TWITTER_ATS by string

    val TARGET_LIST_ID by longOrNull
    val TARGET_LIST_SLUG by stringOrNull

    val SOURCE_LIST_IDS by longList
    val SOURCE_LIST_SLUGS by stringList
    val SOURCE_USER_IDS by longList
    val SOURCE_USER_SCREEN_NAMES by stringList
    val SOURCE_USER_INCLUDE_SELF by boolean

    val DRYRUN by boolean
}

private val string: ReadOnlyProperty<Env, String>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name) ?: error("Env: ${property.name} is not present.")
    }

private val stringOrNull: ReadOnlyProperty<Env, String?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
    }

private val stringList: ReadOnlyProperty<Env, List<String>>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.split(",").orEmpty()
    }

private val longOrNull: ReadOnlyProperty<Env, Long?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.toLongOrNull()
    }

private val longList: ReadOnlyProperty<Env, List<Long>>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.split(",")?.mapNotNull { it.toLongOrNull() }.orEmpty()
    }

private fun String?.toBooleanFazzy(): Boolean {
    return when (this) {
        null -> false
        "1", "yes" -> true
        else -> lowercase().toBoolean()
    }
}

private val boolean: ReadOnlyProperty<Env, Boolean>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name).toBooleanFazzy()
    }

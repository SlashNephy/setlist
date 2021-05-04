package blue.starry.setlist

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import mu.KotlinLogging
import kotlin.time.measureTime
import kotlin.time.seconds

val logger = KotlinLogging.logger("setlist")

suspend fun main() = coroutineScope {
    while (isActive) {
        val taken = measureTime {
            try {
                Setlist.merge()
            } catch (e: CancellationException) {
                return@coroutineScope
            } catch (t: Throwable) {
                logger.error(t) { "Error occurred while merging." }
            }
        }
        logger.trace { "Merge operation finished in $taken." }

        delay(Env.INTERVAL_SECONDS.seconds)
    }
}

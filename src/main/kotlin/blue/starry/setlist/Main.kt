package blue.starry.setlist

import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.time.measureTime
import kotlin.time.minutes

val logger = KotlinLogging.logger("setlist")

suspend fun main() {
    while (true) {
        val taken = measureTime {
            try {
                Setlist.merge()
            } catch (t: Throwable) {
                logger.error(t) { "Error occurred while merging." }
            }
        }
        logger.trace { "Merge operation finished in $taken." }

        delay(3.minutes)
    }
}

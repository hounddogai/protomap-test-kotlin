package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled tire score is in its life cycle. */
enum class ScheduledTireScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled tire score, with what the planners need to act on it. */
data class ScheduledTireScore(
    var code: String,
    var title: String,
    var dock: String,
    var graceDays: Int,
    var ceiling: Double,
    var status: ScheduledTireScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTireScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTireScoreStatus): Boolean {
        when (status) {
            ScheduledTireScoreStatus.CREATED -> return next == ScheduledTireScoreStatus.VALIDATED || next == ScheduledTireScoreStatus.RETIRED
            ScheduledTireScoreStatus.VALIDATED -> return next == ScheduledTireScoreStatus.PUBLISHED || next == ScheduledTireScoreStatus.RETIRED
            ScheduledTireScoreStatus.PUBLISHED -> {
                when (next) {
                    ScheduledTireScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTireScoreStatus) {
        if (!canMoveTo(next)) {
            error("scheduled tire score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round(graceDays.toDouble() * ceiling * 100) / 100

    fun snapshot(): ScheduledTireScore {
        return ScheduledTireScore(
            code = code,
            title = title,
            dock = dock,
            graceDays = graceDays,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled tire score in status created. */
fun newScheduledTireScore(code: String, title: String): ScheduledTireScore {
    return ScheduledTireScore(
        code = code,
        title = title,
        dock = "",
        graceDays = 0,
        ceiling = 0.0,
        status = ScheduledTireScoreStatus.CREATED,
    )
}

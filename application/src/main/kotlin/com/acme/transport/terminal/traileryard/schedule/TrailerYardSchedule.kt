package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard schedule records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard schedule is in its life cycle. */
enum class TrailerYardScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One trailer yard schedule, with what the planners need to act on it. */
data class TrailerYardSchedule(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var budget: Double,
    var category: String,
    var stopCount: Int,
    var status: TrailerYardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardScheduleStatus): Boolean {
        when (status) {
            TrailerYardScheduleStatus.CREATED -> {
                when (next) {
                    TrailerYardScheduleStatus.PUBLISHED -> return true
                }
                return false
            }
            TrailerYardScheduleStatus.VALIDATED -> {
                when (next) {
                    TrailerYardScheduleStatus.PUBLISHED, TrailerYardScheduleStatus.RETIRED -> return true
                }
                return false
            }
            TrailerYardScheduleStatus.PUBLISHED -> return next == TrailerYardScheduleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"trailer yard schedule"} $code"

    fun netValue(): Double = round((expiresAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardSchedule {
        return TrailerYardSchedule(
            code = code,
            title = title,
            expiresAt = expiresAt,
            budget = budget,
            category = category,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer yard schedule in status created. */
fun newTrailerYardSchedule(code: String, title: String): TrailerYardSchedule {
    return TrailerYardSchedule(
        code = code,
        title = title,
        expiresAt = 0,
        budget = 0.0,
        category = "",
        stopCount = 0,
        status = TrailerYardScheduleStatus.CREATED,
    )
}

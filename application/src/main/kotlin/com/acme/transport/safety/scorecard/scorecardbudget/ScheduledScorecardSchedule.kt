package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled scorecard schedule is in its life cycle. */
enum class ScheduledScorecardScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled scorecard schedule as the scorecard team keeps it. */
data class ScheduledScorecardSchedule(
    var code: String,
    var title: String,
    var reviewer: String,
    var limit: Int,
    var source: String,
    var score: Double,
    var tags: MutableList<String>,
    var graceDays: Int,
    var discount: Double,
    var status: ScheduledScorecardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledScorecardScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledScorecardScheduleStatus): Boolean {
        when (status) {
            ScheduledScorecardScheduleStatus.NEW -> return next == ScheduledScorecardScheduleStatus.RUNNING
            ScheduledScorecardScheduleStatus.QUEUED -> {
                when (next) {
                    ScheduledScorecardScheduleStatus.FAILED -> return true
                }
                return false
            }
            ScheduledScorecardScheduleStatus.RUNNING -> return next == ScheduledScorecardScheduleStatus.FAILED
            ScheduledScorecardScheduleStatus.DONE -> {
                when (next) {
                    ScheduledScorecardScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledScorecardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled scorecard schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled scorecard schedule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round(limit.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledScorecardSchedule {
        return ScheduledScorecardSchedule(
            code = code,
            title = title,
            reviewer = reviewer,
            limit = limit,
            source = source,
            score = score,
            tags = tags,
            graceDays = graceDays,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled scorecard schedule in status new. */
fun newScheduledScorecardSchedule(code: String, title: String): ScheduledScorecardSchedule {
    return ScheduledScorecardSchedule(
        code = code,
        title = title,
        reviewer = "",
        limit = 0,
        source = "",
        score = 0.0,
        tags = mutableListOf<String>(),
        graceDays = 0,
        discount = 0.0,
        status = ScheduledScorecardScheduleStatus.NEW,
    )
}

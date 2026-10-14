package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled coaching summary is in its life cycle. */
enum class ScheduledCoachingSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled coaching summary as the coaching team keeps it. */
data class ScheduledCoachingSummary(
    var code: String,
    var name: String,
    var priority: Int,
    var loadKg: Double,
    var routeCode: String,
    var budget: Double,
    var status: ScheduledCoachingSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCoachingSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCoachingSummaryStatus): Boolean {
        when (status) {
            ScheduledCoachingSummaryStatus.OPEN -> return next == ScheduledCoachingSummaryStatus.ARCHIVED
            ScheduledCoachingSummaryStatus.IN_REVIEW -> return next == ScheduledCoachingSummaryStatus.ARCHIVED
            ScheduledCoachingSummaryStatus.RESOLVED -> return next == ScheduledCoachingSummaryStatus.REOPENED
            ScheduledCoachingSummaryStatus.REOPENED -> {
                when (next) {
                    ScheduledCoachingSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCoachingSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled coaching summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round((loadKg - budget) * 100) / 100

    fun snapshot(): ScheduledCoachingSummary {
        return ScheduledCoachingSummary(
            code = code,
            name = name,
            priority = priority,
            loadKg = loadKg,
            routeCode = routeCode,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled coaching summary in status open. */
fun newScheduledCoachingSummary(code: String, name: String): ScheduledCoachingSummary {
    return ScheduledCoachingSummary(
        code = code,
        name = name,
        priority = 0,
        loadKg = 0.0,
        routeCode = "",
        budget = 0.0,
        status = ScheduledCoachingSummaryStatus.OPEN,
    )
}

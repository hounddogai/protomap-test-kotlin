package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.text.normalize

/** Where a redelivery schedule is in its life cycle. */
enum class RedeliveryScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A redelivery schedule as the redelivery team keeps it. */
data class RedeliverySchedule(
    var code: String,
    var name: String,
    var timezone: String,
    var routeCode: String,
    var ratio: Double,
    var status: RedeliveryScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryScheduleStatus): Boolean {
        when (status) {
            RedeliveryScheduleStatus.OPEN -> return next == RedeliveryScheduleStatus.RESOLVED
            RedeliveryScheduleStatus.IN_REVIEW -> {
                when (next) {
                    RedeliveryScheduleStatus.REOPENED -> return true
                }
                return false
            }
            RedeliveryScheduleStatus.RESOLVED -> return next == RedeliveryScheduleStatus.REOPENED || next == RedeliveryScheduleStatus.ARCHIVED
            RedeliveryScheduleStatus.REOPENED -> return next == RedeliveryScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RedeliveryScheduleStatus) {
        if (!canMoveTo(next)) {
            error("redelivery schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"redelivery schedule"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): RedeliverySchedule {
        return RedeliverySchedule(
            code = code,
            name = name,
            timezone = timezone,
            routeCode = routeCode,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery schedule in status open. */
fun newRedeliverySchedule(code: String, name: String): RedeliverySchedule {
    return RedeliverySchedule(
        code = code,
        name = name,
        timezone = "",
        routeCode = "",
        ratio = 0.0,
        status = RedeliveryScheduleStatus.OPEN,
    )
}

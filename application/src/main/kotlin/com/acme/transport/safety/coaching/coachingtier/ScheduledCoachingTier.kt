package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.text.normalize

// The scheduled coaching tier records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a scheduled coaching tier is in its life cycle. */
enum class ScheduledCoachingTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled coaching tier and its current status. */
data class ScheduledCoachingTier(
    var code: String,
    var title: String,
    var endsAt: Int,
    var vehicleId: String,
    var routeCode: String,
    var status: ScheduledCoachingTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCoachingTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCoachingTierStatus): Boolean {
        when (status) {
            ScheduledCoachingTierStatus.NEW -> return next == ScheduledCoachingTierStatus.RUNNING || next == ScheduledCoachingTierStatus.FAILED
            ScheduledCoachingTierStatus.QUEUED -> {
                when (next) {
                    ScheduledCoachingTierStatus.RUNNING, ScheduledCoachingTierStatus.FAILED -> return true
                }
                return false
            }
            ScheduledCoachingTierStatus.RUNNING -> return next == ScheduledCoachingTierStatus.DONE || next == ScheduledCoachingTierStatus.FAILED
            ScheduledCoachingTierStatus.DONE -> {
                when (next) {
                    ScheduledCoachingTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCoachingTierStatus) {
        if (!canMoveTo(next)) {
            error("scheduled coaching tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): ScheduledCoachingTier {
        return ScheduledCoachingTier(
            code = code,
            title = title,
            endsAt = endsAt,
            vehicleId = vehicleId,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled coaching tier in status new. */
fun newScheduledCoachingTier(code: String, title: String): ScheduledCoachingTier {
    return ScheduledCoachingTier(
        code = code,
        title = title,
        endsAt = 0,
        vehicleId = "",
        routeCode = "",
        status = ScheduledCoachingTierStatus.NEW,
    )
}

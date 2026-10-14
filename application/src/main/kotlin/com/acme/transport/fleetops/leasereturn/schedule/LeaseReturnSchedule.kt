package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return schedule records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return schedule is in its life cycle. */
enum class LeaseReturnScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lease return schedule and its current status. */
data class LeaseReturnSchedule(
    var code: String,
    var name: String,
    var active: Boolean,
    var locale: String,
    var ceiling: Double,
    var trailerId: String,
    var depot: String,
    var durationMinutes: Int,
    var status: LeaseReturnScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnScheduleStatus): Boolean {
        when (status) {
            LeaseReturnScheduleStatus.IDLE -> return next == LeaseReturnScheduleStatus.WARMING || next == LeaseReturnScheduleStatus.DRAINING
            LeaseReturnScheduleStatus.WARMING -> return next == LeaseReturnScheduleStatus.STOPPED
            LeaseReturnScheduleStatus.READY -> {
                when (next) {
                    LeaseReturnScheduleStatus.DRAINING, LeaseReturnScheduleStatus.STOPPED -> return true
                }
                return false
            }
            LeaseReturnScheduleStatus.DRAINING -> {
                when (next) {
                    LeaseReturnScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnScheduleStatus) {
        if (!canMoveTo(next)) {
            error("lease return schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round(ceiling * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnSchedule {
        return LeaseReturnSchedule(
            code = code,
            name = name,
            active = active,
            locale = locale,
            ceiling = ceiling,
            trailerId = trailerId,
            depot = depot,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lease return schedule in status idle. */
fun newLeaseReturnSchedule(code: String, name: String): LeaseReturnSchedule {
    return LeaseReturnSchedule(
        code = code,
        name = name,
        active = false,
        locale = "",
        ceiling = 0.0,
        trailerId = "",
        depot = "",
        durationMinutes = 0,
        status = LeaseReturnScheduleStatus.IDLE,
    )
}

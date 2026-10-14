package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dwell schedule records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a scheduled dwell schedule is in its life cycle. */
enum class ScheduledDwellScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled dwell schedule. Its code is unique within a tenant. */
data class ScheduledDwellSchedule(
    var code: String,
    var name: String,
    var odometer: Int,
    var pallets: Int,
    var batchSize: Int,
    var dock: String,
    var status: ScheduledDwellScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDwellScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDwellScheduleStatus): Boolean {
        when (status) {
            ScheduledDwellScheduleStatus.PROPOSED -> return next == ScheduledDwellScheduleStatus.REJECTED
            ScheduledDwellScheduleStatus.APPROVED -> {
                when (next) {
                    ScheduledDwellScheduleStatus.REJECTED, ScheduledDwellScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledDwellScheduleStatus.REJECTED -> return next == ScheduledDwellScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledDwellScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dwell schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round(batchSize.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): ScheduledDwellSchedule {
        return ScheduledDwellSchedule(
            code = code,
            name = name,
            odometer = odometer,
            pallets = pallets,
            batchSize = batchSize,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled dwell schedule in status proposed. */
fun newScheduledDwellSchedule(code: String, name: String): ScheduledDwellSchedule {
    return ScheduledDwellSchedule(
        code = code,
        name = name,
        odometer = 0,
        pallets = 0,
        batchSize = 0,
        dock = "",
        status = ScheduledDwellScheduleStatus.PROPOSED,
    )
}

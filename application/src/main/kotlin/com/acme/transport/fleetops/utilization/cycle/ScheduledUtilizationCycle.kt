package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.text.normalize

// The scheduled utilization cycle records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a scheduled utilization cycle is in its life cycle. */
enum class ScheduledUtilizationCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled utilization cycle. Its code is unique within a tenant. */
data class ScheduledUtilizationCycle(
    var code: String,
    var name: String,
    var driverId: String,
    var attempts: Int,
    var visible: Boolean,
    var checksum: String,
    var label: String,
    var approved: Boolean,
    var status: ScheduledUtilizationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledUtilizationCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledUtilizationCycleStatus): Boolean {
        when (status) {
            ScheduledUtilizationCycleStatus.NEW -> return next == ScheduledUtilizationCycleStatus.RUNNING || next == ScheduledUtilizationCycleStatus.FAILED
            ScheduledUtilizationCycleStatus.QUEUED -> {
                when (next) {
                    ScheduledUtilizationCycleStatus.RUNNING, ScheduledUtilizationCycleStatus.DONE -> return true
                }
                return false
            }
            ScheduledUtilizationCycleStatus.RUNNING -> return next == ScheduledUtilizationCycleStatus.DONE
            ScheduledUtilizationCycleStatus.DONE -> return next == ScheduledUtilizationCycleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledUtilizationCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled utilization cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled utilization cycle"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): ScheduledUtilizationCycle {
        return ScheduledUtilizationCycle(
            code = code,
            name = name,
            driverId = driverId,
            attempts = attempts,
            visible = visible,
            checksum = checksum,
            label = label,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled utilization cycle in status new. */
fun newScheduledUtilizationCycle(code: String, name: String): ScheduledUtilizationCycle {
    return ScheduledUtilizationCycle(
        code = code,
        name = name,
        driverId = "",
        attempts = 0,
        visible = false,
        checksum = "",
        label = "",
        approved = false,
        status = ScheduledUtilizationCycleStatus.NEW,
    )
}

package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.text.normalize

/** Where a utilization slot is in its life cycle. */
enum class UtilizationSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A utilization slot as the utilization team keeps it. */
data class UtilizationSlot(
    var code: String,
    var name: String,
    var driverId: String,
    var trailerId: String,
    var durationMinutes: Int,
    var notes: String,
    var summary: String,
    var status: UtilizationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationSlotStatus): Boolean {
        when (status) {
            UtilizationSlotStatus.PROPOSED -> return next == UtilizationSlotStatus.WITHDRAWN
            UtilizationSlotStatus.APPROVED -> {
                when (next) {
                    UtilizationSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            UtilizationSlotStatus.REJECTED -> {
                when (next) {
                    UtilizationSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationSlotStatus) {
        if (!canMoveTo(next)) {
            error("utilization slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): UtilizationSlot {
        return UtilizationSlot(
            code = code,
            name = name,
            driverId = driverId,
            trailerId = trailerId,
            durationMinutes = durationMinutes,
            notes = notes,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a utilization slot in status proposed. */
fun newUtilizationSlot(code: String, name: String): UtilizationSlot {
    return UtilizationSlot(
        code = code,
        name = name,
        driverId = "",
        trailerId = "",
        durationMinutes = 0,
        notes = "",
        summary = "",
        status = UtilizationSlotStatus.PROPOSED,
    )
}

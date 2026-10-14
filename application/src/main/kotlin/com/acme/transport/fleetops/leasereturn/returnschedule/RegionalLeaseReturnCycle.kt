package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional lease return cycle is in its life cycle. */
enum class RegionalLeaseReturnCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional lease return cycle and its current status. */
data class RegionalLeaseReturnCycle(
    var code: String,
    var label: String,
    var active: Boolean,
    var capacity: Int,
    var summary: String,
    var quantity: Int,
    var unit: String,
    var stopCount: Int,
    var pallets: Int,
    var status: RegionalLeaseReturnCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnCycleStatus): Boolean {
        when (status) {
            RegionalLeaseReturnCycleStatus.PROPOSED -> return next == RegionalLeaseReturnCycleStatus.REJECTED || next == RegionalLeaseReturnCycleStatus.WITHDRAWN
            RegionalLeaseReturnCycleStatus.APPROVED -> {
                when (next) {
                    RegionalLeaseReturnCycleStatus.REJECTED, RegionalLeaseReturnCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalLeaseReturnCycleStatus.REJECTED -> {
                when (next) {
                    RegionalLeaseReturnCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional lease return cycle"} $code"

    fun projectedVolume(): Double = round(stopCount.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): RegionalLeaseReturnCycle {
        return RegionalLeaseReturnCycle(
            code = code,
            label = label,
            active = active,
            capacity = capacity,
            summary = summary,
            quantity = quantity,
            unit = unit,
            stopCount = stopCount,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional lease return cycle in status proposed. */
fun newRegionalLeaseReturnCycle(code: String, label: String): RegionalLeaseReturnCycle {
    return RegionalLeaseReturnCycle(
        code = code,
        label = label,
        active = false,
        capacity = 0,
        summary = "",
        quantity = 0,
        unit = "",
        stopCount = 0,
        pallets = 0,
        status = RegionalLeaseReturnCycleStatus.PROPOSED,
    )
}

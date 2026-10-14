package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional drop allocation records of lastmile's drop work, and the rules, stores, and calculations
// around them.

/** Where a regional drop allocation is in its life cycle. */
enum class RegionalDropAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional drop allocation and its current status. */
data class RegionalDropAllocation(
    var code: String,
    var name: String,
    var loadKg: Double,
    var amount: Double,
    var depot: String,
    var priority: Int,
    var distanceKm: Double,
    var status: RegionalDropAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropAllocationStatus): Boolean {
        when (status) {
            RegionalDropAllocationStatus.PLANNED -> return next == RegionalDropAllocationStatus.COMPLETED || next == RegionalDropAllocationStatus.CANCELLED
            RegionalDropAllocationStatus.SCHEDULED -> return next == RegionalDropAllocationStatus.COMPLETED
            RegionalDropAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalDropAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDropAllocationStatus.COMPLETED -> {
                when (next) {
                    RegionalDropAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDropAllocationStatus) {
        if (!canMoveTo(next)) {
            error("regional drop allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional drop allocation"} $code"

    fun projectedTotal(): Double = round((distanceKm + priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalDropAllocation {
        return RegionalDropAllocation(
            code = code,
            name = name,
            loadKg = loadKg,
            amount = amount,
            depot = depot,
            priority = priority,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional drop allocation in status planned. */
fun newRegionalDropAllocation(code: String, name: String): RegionalDropAllocation {
    return RegionalDropAllocation(
        code = code,
        name = name,
        loadKg = 0.0,
        amount = 0.0,
        depot = "",
        priority = 0,
        distanceKm = 0.0,
        status = RegionalDropAllocationStatus.PLANNED,
    )
}

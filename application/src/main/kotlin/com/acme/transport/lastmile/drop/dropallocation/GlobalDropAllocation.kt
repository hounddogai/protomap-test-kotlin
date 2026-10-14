package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global drop allocation records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a global drop allocation is in its life cycle. */
enum class GlobalDropAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global drop allocation as the drop team keeps it. */
data class GlobalDropAllocation(
    var code: String,
    var title: String,
    var country: String,
    var distanceKm: Double,
    var minimum: Double,
    var odometer: Int,
    var summary: String,
    var status: GlobalDropAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDropAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDropAllocationStatus): Boolean {
        when (status) {
            GlobalDropAllocationStatus.REQUESTED -> return next == GlobalDropAllocationStatus.GRANTED || next == GlobalDropAllocationStatus.REVOKED
            GlobalDropAllocationStatus.GRANTED -> return next == GlobalDropAllocationStatus.REVOKED || next == GlobalDropAllocationStatus.EXPIRED
            GlobalDropAllocationStatus.REVOKED -> return next == GlobalDropAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalDropAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global drop allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((minimum - odometer.toDouble()) * 100) / 100

    fun snapshot(): GlobalDropAllocation {
        return GlobalDropAllocation(
            code = code,
            title = title,
            country = country,
            distanceKm = distanceKm,
            minimum = minimum,
            odometer = odometer,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global drop allocation in status requested. */
fun newGlobalDropAllocation(code: String, title: String): GlobalDropAllocation {
    return GlobalDropAllocation(
        code = code,
        title = title,
        country = "",
        distanceKm = 0.0,
        minimum = 0.0,
        odometer = 0,
        summary = "",
        status = GlobalDropAllocationStatus.REQUESTED,
    )
}

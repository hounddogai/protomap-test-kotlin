package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped cross dock allocation records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a capped cross dock allocation is in its life cycle. */
enum class CappedCrossDockAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped cross dock allocation and its current status. */
data class CappedCrossDockAllocation(
    var code: String,
    var label: String,
    var discount: Double,
    var maximum: Double,
    var distanceKm: Double,
    var vehicleId: String,
    var status: CappedCrossDockAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCrossDockAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCrossDockAllocationStatus): Boolean {
        when (status) {
            CappedCrossDockAllocationStatus.CREATED -> return next == CappedCrossDockAllocationStatus.RETIRED
            CappedCrossDockAllocationStatus.VALIDATED -> return next == CappedCrossDockAllocationStatus.RETIRED
            CappedCrossDockAllocationStatus.PUBLISHED -> return next == CappedCrossDockAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedCrossDockAllocationStatus) {
        if (!canMoveTo(next)) {
            error("capped cross dock allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((discount - maximum) * 100) / 100

    fun snapshot(): CappedCrossDockAllocation {
        return CappedCrossDockAllocation(
            code = code,
            label = label,
            discount = discount,
            maximum = maximum,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped cross dock allocation in status created. */
fun newCappedCrossDockAllocation(code: String, label: String): CappedCrossDockAllocation {
    return CappedCrossDockAllocation(
        code = code,
        label = label,
        discount = 0.0,
        maximum = 0.0,
        distanceKm = 0.0,
        vehicleId = "",
        status = CappedCrossDockAllocationStatus.CREATED,
    )
}

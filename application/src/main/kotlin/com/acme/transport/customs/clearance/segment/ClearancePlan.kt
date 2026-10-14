package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance plan is in its life cycle. */
enum class ClearancePlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A clearance plan as the clearance team keeps it. */
data class ClearancePlan(
    var code: String,
    var label: String,
    var amount: Double,
    var depot: String,
    var pinned: Boolean,
    var distanceKm: Double,
    var fuelLiters: Double,
    var status: ClearancePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearancePlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearancePlanStatus): Boolean {
        when (status) {
            ClearancePlanStatus.PROPOSED -> return next == ClearancePlanStatus.APPROVED
            ClearancePlanStatus.APPROVED -> return next == ClearancePlanStatus.WITHDRAWN
            ClearancePlanStatus.REJECTED -> return next == ClearancePlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ClearancePlanStatus) {
        if (!canMoveTo(next)) {
            error("clearance plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((distanceKm + fuelLiters) * 100) / 100

    fun snapshot(): ClearancePlan {
        return ClearancePlan(
            code = code,
            label = label,
            amount = amount,
            depot = depot,
            pinned = pinned,
            distanceKm = distanceKm,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a clearance plan in status proposed. */
fun newClearancePlan(code: String, label: String): ClearancePlan {
    return ClearancePlan(
        code = code,
        label = label,
        amount = 0.0,
        depot = "",
        pinned = false,
        distanceKm = 0.0,
        fuelLiters = 0.0,
        status = ClearancePlanStatus.PROPOSED,
    )
}

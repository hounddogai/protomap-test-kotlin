package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending ev charge override is in its life cycle. */
enum class PendingEvChargeOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending ev charge override and its current status. */
data class PendingEvChargeOverride(
    var code: String,
    var label: String,
    var driverId: String,
    var fuelLiters: Double,
    var limit: Int,
    var cost: Double,
    var summary: String,
    var status: PendingEvChargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEvChargeOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEvChargeOverrideStatus): Boolean {
        when (status) {
            PendingEvChargeOverrideStatus.REQUESTED -> return next == PendingEvChargeOverrideStatus.REVOKED
            PendingEvChargeOverrideStatus.GRANTED -> return next == PendingEvChargeOverrideStatus.REVOKED
            PendingEvChargeOverrideStatus.REVOKED -> return next == PendingEvChargeOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingEvChargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending ev charge override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending ev charge override"} $code"

    fun adjustedValue(): Double = round((cost - fuelLiters) * 100) / 100

    fun snapshot(): PendingEvChargeOverride {
        return PendingEvChargeOverride(
            code = code,
            label = label,
            driverId = driverId,
            fuelLiters = fuelLiters,
            limit = limit,
            cost = cost,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending ev charge override in status requested. */
fun newPendingEvChargeOverride(code: String, label: String): PendingEvChargeOverride {
    return PendingEvChargeOverride(
        code = code,
        label = label,
        driverId = "",
        fuelLiters = 0.0,
        limit = 0,
        cost = 0.0,
        summary = "",
        status = PendingEvChargeOverrideStatus.REQUESTED,
    )
}

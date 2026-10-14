package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive accessorial adjustment records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a adaptive accessorial adjustment is in its life cycle. */
enum class AdaptiveAccessorialAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive accessorial adjustment as the accessorial team keeps it. */
data class AdaptiveAccessorialAdjustment(
    var code: String,
    var label: String,
    var vehicleId: String,
    var visible: Boolean,
    var retries: Int,
    var distanceKm: Double,
    var country: String,
    var status: AdaptiveAccessorialAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAccessorialAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAccessorialAdjustmentStatus): Boolean {
        when (status) {
            AdaptiveAccessorialAdjustmentStatus.REQUESTED -> return next == AdaptiveAccessorialAdjustmentStatus.GRANTED
            AdaptiveAccessorialAdjustmentStatus.GRANTED -> return next == AdaptiveAccessorialAdjustmentStatus.REVOKED || next == AdaptiveAccessorialAdjustmentStatus.EXPIRED
            AdaptiveAccessorialAdjustmentStatus.REVOKED -> return next == AdaptiveAccessorialAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveAccessorialAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive accessorial adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive accessorial adjustment"} $code"

    fun projectedExposure(): Double = round((retries.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): AdaptiveAccessorialAdjustment {
        return AdaptiveAccessorialAdjustment(
            code = code,
            label = label,
            vehicleId = vehicleId,
            visible = visible,
            retries = retries,
            distanceKm = distanceKm,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive accessorial adjustment in status requested. */
fun newAdaptiveAccessorialAdjustment(code: String, label: String): AdaptiveAccessorialAdjustment {
    return AdaptiveAccessorialAdjustment(
        code = code,
        label = label,
        vehicleId = "",
        visible = false,
        retries = 0,
        distanceKm = 0.0,
        country = "",
        status = AdaptiveAccessorialAdjustmentStatus.REQUESTED,
    )
}

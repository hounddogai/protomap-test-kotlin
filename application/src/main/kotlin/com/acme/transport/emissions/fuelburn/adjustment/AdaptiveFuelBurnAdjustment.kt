package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive fuel burn adjustment records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a adaptive fuel burn adjustment is in its life cycle. */
enum class AdaptiveFuelBurnAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive fuel burn adjustment. Its code is unique within a tenant. */
data class AdaptiveFuelBurnAdjustment(
    var code: String,
    var label: String,
    var driverId: String,
    var locale: String,
    var minimum: Double,
    var retries: Int,
    var createdAt: Int,
    var discount: Double,
    var revision: Int,
    var status: AdaptiveFuelBurnAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFuelBurnAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFuelBurnAdjustmentStatus): Boolean {
        when (status) {
            AdaptiveFuelBurnAdjustmentStatus.REQUESTED -> {
                when (next) {
                    AdaptiveFuelBurnAdjustmentStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveFuelBurnAdjustmentStatus.GRANTED -> {
                when (next) {
                    AdaptiveFuelBurnAdjustmentStatus.REVOKED, AdaptiveFuelBurnAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveFuelBurnAdjustmentStatus.REVOKED -> {
                when (next) {
                    AdaptiveFuelBurnAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveFuelBurnAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive fuel burn adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round((discount + revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveFuelBurnAdjustment {
        return AdaptiveFuelBurnAdjustment(
            code = code,
            label = label,
            driverId = driverId,
            locale = locale,
            minimum = minimum,
            retries = retries,
            createdAt = createdAt,
            discount = discount,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive fuel burn adjustment in status requested. */
fun newAdaptiveFuelBurnAdjustment(code: String, label: String): AdaptiveFuelBurnAdjustment {
    return AdaptiveFuelBurnAdjustment(
        code = code,
        label = label,
        driverId = "",
        locale = "",
        minimum = 0.0,
        retries = 0,
        createdAt = 0,
        discount = 0.0,
        revision = 0,
        status = AdaptiveFuelBurnAdjustmentStatus.REQUESTED,
    )
}

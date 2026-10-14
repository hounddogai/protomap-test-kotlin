package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered fuel surcharge baseline records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a tiered fuel surcharge baseline is in its life cycle. */
enum class TieredFuelSurchargeBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One tiered fuel surcharge baseline, with what the planners need to act on it. */
data class TieredFuelSurchargeBaseline(
    var code: String,
    var label: String,
    var archived: Boolean,
    var ownerId: String,
    var currency: String,
    var pinned: Boolean,
    var unit: String,
    var score: Double,
    var carrier: String,
    var phone: String,
    var distanceKm: Double,
    var status: TieredFuelSurchargeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelSurchargeBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelSurchargeBaselineStatus): Boolean {
        when (status) {
            TieredFuelSurchargeBaselineStatus.PROPOSED -> {
                when (next) {
                    TieredFuelSurchargeBaselineStatus.REJECTED -> return true
                }
                return false
            }
            TieredFuelSurchargeBaselineStatus.APPROVED -> {
                when (next) {
                    TieredFuelSurchargeBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredFuelSurchargeBaselineStatus.REJECTED -> {
                when (next) {
                    TieredFuelSurchargeBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelSurchargeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel surcharge baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((score - distanceKm) * 100) / 100

    fun snapshot(): TieredFuelSurchargeBaseline {
        return TieredFuelSurchargeBaseline(
            code = code,
            label = label,
            archived = archived,
            ownerId = ownerId,
            currency = currency,
            pinned = pinned,
            unit = unit,
            score = score,
            carrier = carrier,
            phone = phone,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered fuel surcharge baseline in status proposed. */
fun newTieredFuelSurchargeBaseline(code: String, label: String): TieredFuelSurchargeBaseline {
    return TieredFuelSurchargeBaseline(
        code = code,
        label = label,
        archived = false,
        ownerId = "",
        currency = "",
        pinned = false,
        unit = "",
        score = 0.0,
        carrier = "",
        phone = "",
        distanceKm = 0.0,
        status = TieredFuelSurchargeBaselineStatus.PROPOSED,
    )
}

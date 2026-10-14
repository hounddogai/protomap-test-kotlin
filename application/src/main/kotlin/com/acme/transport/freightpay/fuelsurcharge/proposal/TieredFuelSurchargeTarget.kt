package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered fuel surcharge target is in its life cycle. */
enum class TieredFuelSurchargeTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered fuel surcharge target and its current status. */
data class TieredFuelSurchargeTarget(
    var code: String,
    var label: String,
    var pallets: Int,
    var threshold: Double,
    var rate: Double,
    var active: Boolean,
    var status: TieredFuelSurchargeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelSurchargeTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelSurchargeTargetStatus): Boolean {
        when (status) {
            TieredFuelSurchargeTargetStatus.OPEN -> {
                when (next) {
                    TieredFuelSurchargeTargetStatus.REOPENED -> return true
                }
                return false
            }
            TieredFuelSurchargeTargetStatus.IN_REVIEW -> {
                when (next) {
                    TieredFuelSurchargeTargetStatus.RESOLVED, TieredFuelSurchargeTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredFuelSurchargeTargetStatus.RESOLVED -> {
                when (next) {
                    TieredFuelSurchargeTargetStatus.REOPENED -> return true
                }
                return false
            }
            TieredFuelSurchargeTargetStatus.REOPENED -> return next == TieredFuelSurchargeTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredFuelSurchargeTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel surcharge target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round((rate + pallets.toDouble()) * 100) / 100

    fun snapshot(): TieredFuelSurchargeTarget {
        return TieredFuelSurchargeTarget(
            code = code,
            label = label,
            pallets = pallets,
            threshold = threshold,
            rate = rate,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered fuel surcharge target in status open. */
fun newTieredFuelSurchargeTarget(code: String, label: String): TieredFuelSurchargeTarget {
    return TieredFuelSurchargeTarget(
        code = code,
        label = label,
        pallets = 0,
        threshold = 0.0,
        rate = 0.0,
        active = false,
        status = TieredFuelSurchargeTargetStatus.OPEN,
    )
}

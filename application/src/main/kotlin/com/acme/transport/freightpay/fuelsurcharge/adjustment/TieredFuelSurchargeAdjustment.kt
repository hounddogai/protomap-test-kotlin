package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered fuel surcharge adjustment records of freightpay's fuel surcharge work, and the rules, stores,
// and calculations around them.

/** Where a tiered fuel surcharge adjustment is in its life cycle. */
enum class TieredFuelSurchargeAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered fuel surcharge adjustment and its current status. */
data class TieredFuelSurchargeAdjustment(
    var code: String,
    var name: String,
    var phone: String,
    var currency: String,
    var attempts: Int,
    var pallets: Int,
    var rate: Double,
    var startsAt: Int,
    var dock: String,
    var ratio: Double,
    var sequence: Int,
    var status: TieredFuelSurchargeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelSurchargeAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelSurchargeAdjustmentStatus): Boolean {
        when (status) {
            TieredFuelSurchargeAdjustmentStatus.PROPOSED -> {
                when (next) {
                    TieredFuelSurchargeAdjustmentStatus.APPROVED, TieredFuelSurchargeAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredFuelSurchargeAdjustmentStatus.APPROVED -> {
                when (next) {
                    TieredFuelSurchargeAdjustmentStatus.REJECTED, TieredFuelSurchargeAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredFuelSurchargeAdjustmentStatus.REJECTED -> {
                when (next) {
                    TieredFuelSurchargeAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelSurchargeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel surcharge adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round((attempts.toDouble() + rate) * 100) / 100

    fun snapshot(): TieredFuelSurchargeAdjustment {
        return TieredFuelSurchargeAdjustment(
            code = code,
            name = name,
            phone = phone,
            currency = currency,
            attempts = attempts,
            pallets = pallets,
            rate = rate,
            startsAt = startsAt,
            dock = dock,
            ratio = ratio,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered fuel surcharge adjustment in status proposed. */
fun newTieredFuelSurchargeAdjustment(code: String, name: String): TieredFuelSurchargeAdjustment {
    return TieredFuelSurchargeAdjustment(
        code = code,
        name = name,
        phone = "",
        currency = "",
        attempts = 0,
        pallets = 0,
        rate = 0.0,
        startsAt = 0,
        dock = "",
        ratio = 0.0,
        sequence = 0,
        status = TieredFuelSurchargeAdjustmentStatus.PROPOSED,
    )
}

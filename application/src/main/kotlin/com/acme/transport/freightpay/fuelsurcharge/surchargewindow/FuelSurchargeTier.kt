package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge tier is in its life cycle. */
enum class FuelSurchargeTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fuel surcharge tier, with what the planners need to act on it. */
data class FuelSurchargeTier(
    var code: String,
    var label: String,
    var currency: String,
    var revision: Int,
    var active: Boolean,
    var quantity: Int,
    var loadKg: Double,
    var phone: String,
    var segment: String,
    var status: FuelSurchargeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeTierStatus): Boolean {
        when (status) {
            FuelSurchargeTierStatus.IDLE -> {
                when (next) {
                    FuelSurchargeTierStatus.READY, FuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
            FuelSurchargeTierStatus.WARMING -> {
                when (next) {
                    FuelSurchargeTierStatus.DRAINING -> return true
                }
                return false
            }
            FuelSurchargeTierStatus.READY -> {
                when (next) {
                    FuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
            FuelSurchargeTierStatus.DRAINING -> {
                when (next) {
                    FuelSurchargeTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeTierStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"fuel surcharge tier"} $code"

    fun weightedValue(): Double = round((quantity.toDouble() + loadKg) * 100) / 100

    fun snapshot(): FuelSurchargeTier {
        return FuelSurchargeTier(
            code = code,
            label = label,
            currency = currency,
            revision = revision,
            active = active,
            quantity = quantity,
            loadKg = loadKg,
            phone = phone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fuel surcharge tier in status idle. */
fun newFuelSurchargeTier(code: String, label: String): FuelSurchargeTier {
    return FuelSurchargeTier(
        code = code,
        label = label,
        currency = "",
        revision = 0,
        active = false,
        quantity = 0,
        loadKg = 0.0,
        phone = "",
        segment = "",
        status = FuelSurchargeTierStatus.IDLE,
    )
}

package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel surcharge window is in its life cycle. */
enum class LenientFuelSurchargeWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient fuel surcharge window. Its code is unique within a tenant. */
data class LenientFuelSurchargeWindow(
    var code: String,
    var name: String,
    var limit: Int,
    var unit: String,
    var summary: String,
    var score: Double,
    var amount: Double,
    var createdAt: Int,
    var status: LenientFuelSurchargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelSurchargeWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelSurchargeWindowStatus): Boolean {
        when (status) {
            LenientFuelSurchargeWindowStatus.PLANNED -> {
                when (next) {
                    LenientFuelSurchargeWindowStatus.IN_PROGRESS, LenientFuelSurchargeWindowStatus.CANCELLED -> return true
                }
                return false
            }
            LenientFuelSurchargeWindowStatus.SCHEDULED -> return next == LenientFuelSurchargeWindowStatus.COMPLETED || next == LenientFuelSurchargeWindowStatus.CANCELLED
            LenientFuelSurchargeWindowStatus.IN_PROGRESS -> return next == LenientFuelSurchargeWindowStatus.COMPLETED
            LenientFuelSurchargeWindowStatus.COMPLETED -> return next == LenientFuelSurchargeWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientFuelSurchargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel surcharge window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient fuel surcharge window"} $code"

    fun adjustedVolume(): Double = round(amount * limit.toDouble() * 100) / 100

    fun snapshot(): LenientFuelSurchargeWindow {
        return LenientFuelSurchargeWindow(
            code = code,
            name = name,
            limit = limit,
            unit = unit,
            summary = summary,
            score = score,
            amount = amount,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient fuel surcharge window in status planned. */
fun newLenientFuelSurchargeWindow(code: String, name: String): LenientFuelSurchargeWindow {
    return LenientFuelSurchargeWindow(
        code = code,
        name = name,
        limit = 0,
        unit = "",
        summary = "",
        score = 0.0,
        amount = 0.0,
        createdAt = 0,
        status = LenientFuelSurchargeWindowStatus.PLANNED,
    )
}

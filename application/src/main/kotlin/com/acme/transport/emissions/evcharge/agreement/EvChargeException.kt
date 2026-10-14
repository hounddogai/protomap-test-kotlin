package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge exception is in its life cycle. */
enum class EvChargeExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a ev charge exception and its current status. */
data class EvChargeException(
    var code: String,
    var label: String,
    var endsAt: Int,
    var floorValue: Double,
    var quantity: Int,
    var amount: Double,
    var budget: Double,
    var minimum: Double,
    var status: EvChargeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeExceptionStatus): Boolean {
        when (status) {
            EvChargeExceptionStatus.PROPOSED -> return next == EvChargeExceptionStatus.APPROVED
            EvChargeExceptionStatus.APPROVED -> return next == EvChargeExceptionStatus.WITHDRAWN
            EvChargeExceptionStatus.REJECTED -> {
                when (next) {
                    EvChargeExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("ev charge exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((amount - endsAt.toDouble()) * 100) / 100

    fun snapshot(): EvChargeException {
        return EvChargeException(
            code = code,
            label = label,
            endsAt = endsAt,
            floorValue = floorValue,
            quantity = quantity,
            amount = amount,
            budget = budget,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a ev charge exception in status proposed. */
fun newEvChargeException(code: String, label: String): EvChargeException {
    return EvChargeException(
        code = code,
        label = label,
        endsAt = 0,
        floorValue = 0.0,
        quantity = 0,
        amount = 0.0,
        budget = 0.0,
        minimum = 0.0,
        status = EvChargeExceptionStatus.PROPOSED,
    )
}

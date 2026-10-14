package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice plan is in its life cycle. */
enum class FreightInvoicePlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One freight invoice plan, with what the planners need to act on it. */
data class FreightInvoicePlan(
    var code: String,
    var label: String,
    var quantity: Int,
    var stopCount: Int,
    var sequence: Int,
    var budget: Double,
    var threshold: Double,
    var graceDays: Int,
    var floorValue: Double,
    var status: FreightInvoicePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoicePlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoicePlanStatus): Boolean {
        when (status) {
            FreightInvoicePlanStatus.IDLE -> return next == FreightInvoicePlanStatus.WARMING || next == FreightInvoicePlanStatus.READY
            FreightInvoicePlanStatus.WARMING -> {
                when (next) {
                    FreightInvoicePlanStatus.STOPPED -> return true
                }
                return false
            }
            FreightInvoicePlanStatus.READY -> return next == FreightInvoicePlanStatus.DRAINING || next == FreightInvoicePlanStatus.STOPPED
            FreightInvoicePlanStatus.DRAINING -> return next == FreightInvoicePlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FreightInvoicePlanStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round(budget * quantity.toDouble() * 100) / 100

    fun snapshot(): FreightInvoicePlan {
        return FreightInvoicePlan(
            code = code,
            label = label,
            quantity = quantity,
            stopCount = stopCount,
            sequence = sequence,
            budget = budget,
            threshold = threshold,
            graceDays = graceDays,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a freight invoice plan in status idle. */
fun newFreightInvoicePlan(code: String, label: String): FreightInvoicePlan {
    return FreightInvoicePlan(
        code = code,
        label = label,
        quantity = 0,
        stopCount = 0,
        sequence = 0,
        budget = 0.0,
        threshold = 0.0,
        graceDays = 0,
        floorValue = 0.0,
        status = FreightInvoicePlanStatus.IDLE,
    )
}

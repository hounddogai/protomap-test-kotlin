package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.text.normalize

/** Where a freight invoice budget is in its life cycle. */
enum class FreightInvoiceBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One freight invoice budget, with what the planners need to act on it. */
data class FreightInvoiceBudget(
    var code: String,
    var label: String,
    var unit: String,
    var carrier: String,
    var description: String,
    var category: String,
    var trailerId: String,
    var odometer: Int,
    var status: FreightInvoiceBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceBudgetStatus): Boolean {
        when (status) {
            FreightInvoiceBudgetStatus.REQUESTED -> return next == FreightInvoiceBudgetStatus.GRANTED || next == FreightInvoiceBudgetStatus.EXPIRED
            FreightInvoiceBudgetStatus.GRANTED -> return next == FreightInvoiceBudgetStatus.REVOKED || next == FreightInvoiceBudgetStatus.EXPIRED
            FreightInvoiceBudgetStatus.REVOKED -> return next == FreightInvoiceBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceBudgetStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"freight invoice budget"} $code"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): FreightInvoiceBudget {
        return FreightInvoiceBudget(
            code = code,
            label = label,
            unit = unit,
            carrier = carrier,
            description = description,
            category = category,
            trailerId = trailerId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a freight invoice budget in status requested. */
fun newFreightInvoiceBudget(code: String, label: String): FreightInvoiceBudget {
    return FreightInvoiceBudget(
        code = code,
        label = label,
        unit = "",
        carrier = "",
        description = "",
        category = "",
        trailerId = "",
        odometer = 0,
        status = FreightInvoiceBudgetStatus.REQUESTED,
    )
}

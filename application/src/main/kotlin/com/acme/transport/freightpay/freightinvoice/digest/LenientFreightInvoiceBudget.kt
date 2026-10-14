package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient freight invoice budget is in its life cycle. */
enum class LenientFreightInvoiceBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient freight invoice budget. Its code is unique within a tenant. */
data class LenientFreightInvoiceBudget(
    var code: String,
    var name: String,
    var unit: String,
    var reviewer: String,
    var segment: String,
    var quantity: Int,
    var capacity: Int,
    var pallets: Int,
    var sequence: Int,
    var etaMinutes: Int,
    var status: LenientFreightInvoiceBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFreightInvoiceBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFreightInvoiceBudgetStatus): Boolean {
        when (status) {
            LenientFreightInvoiceBudgetStatus.OPEN -> {
                when (next) {
                    LenientFreightInvoiceBudgetStatus.RESOLVED -> return true
                }
                return false
            }
            LenientFreightInvoiceBudgetStatus.IN_REVIEW -> {
                when (next) {
                    LenientFreightInvoiceBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientFreightInvoiceBudgetStatus.RESOLVED -> return next == LenientFreightInvoiceBudgetStatus.ARCHIVED
            LenientFreightInvoiceBudgetStatus.REOPENED -> {
                when (next) {
                    LenientFreightInvoiceBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFreightInvoiceBudgetStatus) {
        if (!canMoveTo(next)) {
            error("lenient freight invoice budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round((quantity.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): LenientFreightInvoiceBudget {
        return LenientFreightInvoiceBudget(
            code = code,
            name = name,
            unit = unit,
            reviewer = reviewer,
            segment = segment,
            quantity = quantity,
            capacity = capacity,
            pallets = pallets,
            sequence = sequence,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient freight invoice budget in status open. */
fun newLenientFreightInvoiceBudget(code: String, name: String): LenientFreightInvoiceBudget {
    return LenientFreightInvoiceBudget(
        code = code,
        name = name,
        unit = "",
        reviewer = "",
        segment = "",
        quantity = 0,
        capacity = 0,
        pallets = 0,
        sequence = 0,
        etaMinutes = 0,
        status = LenientFreightInvoiceBudgetStatus.OPEN,
    )
}

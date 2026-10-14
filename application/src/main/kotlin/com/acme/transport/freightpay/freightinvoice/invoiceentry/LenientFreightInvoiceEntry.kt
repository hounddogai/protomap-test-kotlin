package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.text.normalize

// The lenient freight invoice entry records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a lenient freight invoice entry is in its life cycle. */
enum class LenientFreightInvoiceEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient freight invoice entry and its current status. */
data class LenientFreightInvoiceEntry(
    var code: String,
    var name: String,
    var visible: Boolean,
    var fuelLiters: Double,
    var lane: String,
    var segment: String,
    var status: LenientFreightInvoiceEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFreightInvoiceEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFreightInvoiceEntryStatus): Boolean {
        when (status) {
            LenientFreightInvoiceEntryStatus.OPEN -> {
                when (next) {
                    LenientFreightInvoiceEntryStatus.RESOLVED -> return true
                }
                return false
            }
            LenientFreightInvoiceEntryStatus.IN_REVIEW -> {
                when (next) {
                    LenientFreightInvoiceEntryStatus.RESOLVED, LenientFreightInvoiceEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientFreightInvoiceEntryStatus.RESOLVED -> {
                when (next) {
                    LenientFreightInvoiceEntryStatus.REOPENED, LenientFreightInvoiceEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientFreightInvoiceEntryStatus.REOPENED -> return next == LenientFreightInvoiceEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientFreightInvoiceEntryStatus) {
        if (!canMoveTo(next)) {
            error("lenient freight invoice entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): LenientFreightInvoiceEntry {
        return LenientFreightInvoiceEntry(
            code = code,
            name = name,
            visible = visible,
            fuelLiters = fuelLiters,
            lane = lane,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient freight invoice entry in status open. */
fun newLenientFreightInvoiceEntry(code: String, name: String): LenientFreightInvoiceEntry {
    return LenientFreightInvoiceEntry(
        code = code,
        name = name,
        visible = false,
        fuelLiters = 0.0,
        lane = "",
        segment = "",
        status = LenientFreightInvoiceEntryStatus.OPEN,
    )
}

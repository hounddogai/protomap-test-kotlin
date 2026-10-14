package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.normalize

/** Where a freight invoice request is in its life cycle. */
enum class FreightInvoiceRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a freight invoice request and its current status. */
data class FreightInvoiceRequest(
    var code: String,
    var title: String,
    var summary: String,
    var revision: Int,
    var notes: String,
    var status: FreightInvoiceRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceRequestStatus): Boolean {
        when (status) {
            FreightInvoiceRequestStatus.PROPOSED -> return next == FreightInvoiceRequestStatus.REJECTED || next == FreightInvoiceRequestStatus.WITHDRAWN
            FreightInvoiceRequestStatus.APPROVED -> return next == FreightInvoiceRequestStatus.REJECTED || next == FreightInvoiceRequestStatus.WITHDRAWN
            FreightInvoiceRequestStatus.REJECTED -> return next == FreightInvoiceRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FreightInvoiceRequestStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): FreightInvoiceRequest {
        return FreightInvoiceRequest(
            code = code,
            title = title,
            summary = summary,
            revision = revision,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a freight invoice request in status proposed. */
fun newFreightInvoiceRequest(code: String, title: String): FreightInvoiceRequest {
    return FreightInvoiceRequest(
        code = code,
        title = title,
        summary = "",
        revision = 0,
        notes = "",
        status = FreightInvoiceRequestStatus.PROPOSED,
    )
}

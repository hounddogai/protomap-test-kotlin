package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.text.normalize

/** Where a freight invoice draft is in its life cycle. */
enum class FreightInvoiceDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A freight invoice draft as the freight invoice team keeps it. */
data class FreightInvoiceDraft(
    var code: String,
    var label: String,
    var externalRef: String,
    var archived: Boolean,
    var notes: String,
    var locale: String,
    var source: String,
    var attempts: Int,
    var status: FreightInvoiceDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceDraftStatus): Boolean {
        when (status) {
            FreightInvoiceDraftStatus.DRAFT -> return next == FreightInvoiceDraftStatus.ACTIVE
            FreightInvoiceDraftStatus.PENDING -> {
                when (next) {
                    FreightInvoiceDraftStatus.CLOSED -> return true
                }
                return false
            }
            FreightInvoiceDraftStatus.ACTIVE -> return next == FreightInvoiceDraftStatus.SUSPENDED
            FreightInvoiceDraftStatus.SUSPENDED -> return next == FreightInvoiceDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceDraftStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): FreightInvoiceDraft {
        return FreightInvoiceDraft(
            code = code,
            label = label,
            externalRef = externalRef,
            archived = archived,
            notes = notes,
            locale = locale,
            source = source,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a freight invoice draft in status draft. */
fun newFreightInvoiceDraft(code: String, label: String): FreightInvoiceDraft {
    return FreightInvoiceDraft(
        code = code,
        label = label,
        externalRef = "",
        archived = false,
        notes = "",
        locale = "",
        source = "",
        attempts = 0,
        status = FreightInvoiceDraftStatus.DRAFT,
    )
}

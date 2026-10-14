package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.text.normalize

/** Where a lenient freight invoice draft is in its life cycle. */
enum class LenientFreightInvoiceDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lenient freight invoice draft, with what the planners need to act on it. */
data class LenientFreightInvoiceDraft(
    var code: String,
    var label: String,
    var active: Boolean,
    var summary: String,
    var revision: Int,
    var status: LenientFreightInvoiceDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFreightInvoiceDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFreightInvoiceDraftStatus): Boolean {
        when (status) {
            LenientFreightInvoiceDraftStatus.PROPOSED -> {
                when (next) {
                    LenientFreightInvoiceDraftStatus.REJECTED -> return true
                }
                return false
            }
            LenientFreightInvoiceDraftStatus.APPROVED -> return next == LenientFreightInvoiceDraftStatus.REJECTED || next == LenientFreightInvoiceDraftStatus.WITHDRAWN
            LenientFreightInvoiceDraftStatus.REJECTED -> {
                when (next) {
                    LenientFreightInvoiceDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFreightInvoiceDraftStatus) {
        if (!canMoveTo(next)) {
            error("lenient freight invoice draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): LenientFreightInvoiceDraft {
        return LenientFreightInvoiceDraft(
            code = code,
            label = label,
            active = active,
            summary = summary,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient freight invoice draft in status proposed. */
fun newLenientFreightInvoiceDraft(code: String, label: String): LenientFreightInvoiceDraft {
    return LenientFreightInvoiceDraft(
        code = code,
        label = label,
        active = false,
        summary = "",
        revision = 0,
        status = LenientFreightInvoiceDraftStatus.PROPOSED,
    )
}

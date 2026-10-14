package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice review is in its life cycle. */
enum class FreightInvoiceReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One freight invoice review, with what the planners need to act on it. */
data class FreightInvoiceReview(
    var code: String,
    var name: String,
    var channel: String,
    var budget: Double,
    var quantity: Int,
    var sequence: Int,
    var archived: Boolean,
    var graceDays: Int,
    var externalRef: String,
    var status: FreightInvoiceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceReviewStatus): Boolean {
        when (status) {
            FreightInvoiceReviewStatus.PROPOSED -> return next == FreightInvoiceReviewStatus.REJECTED || next == FreightInvoiceReviewStatus.WITHDRAWN
            FreightInvoiceReviewStatus.APPROVED -> return next == FreightInvoiceReviewStatus.REJECTED
            FreightInvoiceReviewStatus.REJECTED -> {
                when (next) {
                    FreightInvoiceReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceReviewStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"freight invoice review"} $code"

    fun weightedTotal(): Double = round((graceDays.toDouble() + budget) * 100) / 100

    fun snapshot(): FreightInvoiceReview {
        return FreightInvoiceReview(
            code = code,
            name = name,
            channel = channel,
            budget = budget,
            quantity = quantity,
            sequence = sequence,
            archived = archived,
            graceDays = graceDays,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a freight invoice review in status proposed. */
fun newFreightInvoiceReview(code: String, name: String): FreightInvoiceReview {
    return FreightInvoiceReview(
        code = code,
        name = name,
        channel = "",
        budget = 0.0,
        quantity = 0,
        sequence = 0,
        archived = false,
        graceDays = 0,
        externalRef = "",
        status = FreightInvoiceReviewStatus.PROPOSED,
    )
}

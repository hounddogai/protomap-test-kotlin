package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual freight invoice review records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a manual freight invoice review is in its life cycle. */
enum class ManualFreightInvoiceReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual freight invoice review. Its code is unique within a tenant. */
data class ManualFreightInvoiceReview(
    var code: String,
    var name: String,
    var cost: Double,
    var capacity: Int,
    var attempts: Int,
    var status: ManualFreightInvoiceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFreightInvoiceReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFreightInvoiceReviewStatus): Boolean {
        when (status) {
            ManualFreightInvoiceReviewStatus.PLANNED -> return next == ManualFreightInvoiceReviewStatus.COMPLETED || next == ManualFreightInvoiceReviewStatus.CANCELLED
            ManualFreightInvoiceReviewStatus.SCHEDULED -> {
                when (next) {
                    ManualFreightInvoiceReviewStatus.COMPLETED -> return true
                }
                return false
            }
            ManualFreightInvoiceReviewStatus.IN_PROGRESS -> return next == ManualFreightInvoiceReviewStatus.COMPLETED || next == ManualFreightInvoiceReviewStatus.CANCELLED
            ManualFreightInvoiceReviewStatus.COMPLETED -> {
                when (next) {
                    ManualFreightInvoiceReviewStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualFreightInvoiceReviewStatus) {
        if (!canMoveTo(next)) {
            error("manual freight invoice review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual freight invoice review"} $code"

    fun effectiveTotal(): Double = round((attempts.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualFreightInvoiceReview {
        return ManualFreightInvoiceReview(
            code = code,
            name = name,
            cost = cost,
            capacity = capacity,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual freight invoice review in status planned. */
fun newManualFreightInvoiceReview(code: String, name: String): ManualFreightInvoiceReview {
    return ManualFreightInvoiceReview(
        code = code,
        name = name,
        cost = 0.0,
        capacity = 0,
        attempts = 0,
        status = ManualFreightInvoiceReviewStatus.PLANNED,
    )
}

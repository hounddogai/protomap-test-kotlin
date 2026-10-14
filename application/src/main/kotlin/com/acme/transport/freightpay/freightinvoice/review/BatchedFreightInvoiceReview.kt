package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched freight invoice review records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a batched freight invoice review is in its life cycle. */
enum class BatchedFreightInvoiceReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched freight invoice review. Its code is unique within a tenant. */
data class BatchedFreightInvoiceReview(
    var code: String,
    var title: String,
    var routeCode: String,
    var revision: Int,
    var source: String,
    var country: String,
    var sequence: Int,
    var status: BatchedFreightInvoiceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFreightInvoiceReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFreightInvoiceReviewStatus): Boolean {
        when (status) {
            BatchedFreightInvoiceReviewStatus.OPEN -> return next == BatchedFreightInvoiceReviewStatus.RESOLVED || next == BatchedFreightInvoiceReviewStatus.ARCHIVED
            BatchedFreightInvoiceReviewStatus.IN_REVIEW -> {
                when (next) {
                    BatchedFreightInvoiceReviewStatus.RESOLVED, BatchedFreightInvoiceReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedFreightInvoiceReviewStatus.RESOLVED -> {
                when (next) {
                    BatchedFreightInvoiceReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedFreightInvoiceReviewStatus.REOPENED -> {
                when (next) {
                    BatchedFreightInvoiceReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFreightInvoiceReviewStatus) {
        if (!canMoveTo(next)) {
            error("batched freight invoice review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((revision.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): BatchedFreightInvoiceReview {
        return BatchedFreightInvoiceReview(
            code = code,
            title = title,
            routeCode = routeCode,
            revision = revision,
            source = source,
            country = country,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched freight invoice review in status open. */
fun newBatchedFreightInvoiceReview(code: String, title: String): BatchedFreightInvoiceReview {
    return BatchedFreightInvoiceReview(
        code = code,
        title = title,
        routeCode = "",
        revision = 0,
        source = "",
        country = "",
        sequence = 0,
        status = BatchedFreightInvoiceReviewStatus.OPEN,
    )
}

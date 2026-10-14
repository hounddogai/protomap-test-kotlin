package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default freight invoice review records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a default freight invoice review is in its life cycle. */
enum class DefaultFreightInvoiceReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default freight invoice review. Its code is unique within a tenant. */
data class DefaultFreightInvoiceReview(
    var code: String,
    var label: String,
    var limit: Int,
    var vehicleId: String,
    var windowDays: Int,
    var discount: Double,
    var archived: Boolean,
    var margin: Double,
    var graceDays: Int,
    var shiftCode: String,
    var segment: String,
    var status: DefaultFreightInvoiceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultFreightInvoiceReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultFreightInvoiceReviewStatus): Boolean {
        when (status) {
            DefaultFreightInvoiceReviewStatus.REQUESTED -> return next == DefaultFreightInvoiceReviewStatus.GRANTED || next == DefaultFreightInvoiceReviewStatus.EXPIRED
            DefaultFreightInvoiceReviewStatus.GRANTED -> {
                when (next) {
                    DefaultFreightInvoiceReviewStatus.REVOKED -> return true
                }
                return false
            }
            DefaultFreightInvoiceReviewStatus.REVOKED -> {
                when (next) {
                    DefaultFreightInvoiceReviewStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultFreightInvoiceReviewStatus) {
        if (!canMoveTo(next)) {
            error("default freight invoice review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((limit.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultFreightInvoiceReview {
        return DefaultFreightInvoiceReview(
            code = code,
            label = label,
            limit = limit,
            vehicleId = vehicleId,
            windowDays = windowDays,
            discount = discount,
            archived = archived,
            margin = margin,
            graceDays = graceDays,
            shiftCode = shiftCode,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default freight invoice review in status requested. */
fun newDefaultFreightInvoiceReview(code: String, label: String): DefaultFreightInvoiceReview {
    return DefaultFreightInvoiceReview(
        code = code,
        label = label,
        limit = 0,
        vehicleId = "",
        windowDays = 0,
        discount = 0.0,
        archived = false,
        margin = 0.0,
        graceDays = 0,
        shiftCode = "",
        segment = "",
        status = DefaultFreightInvoiceReviewStatus.REQUESTED,
    )
}

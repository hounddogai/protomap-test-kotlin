package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling freight invoice request is in its life cycle. */
enum class RollingFreightInvoiceRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling freight invoice request and its current status. */
data class RollingFreightInvoiceRequest(
    var code: String,
    var title: String,
    var margin: Double,
    var threshold: Double,
    var ownerId: String,
    var retries: Int,
    var odometer: Int,
    var lane: String,
    var ratio: Double,
    var status: RollingFreightInvoiceRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFreightInvoiceRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFreightInvoiceRequestStatus): Boolean {
        when (status) {
            RollingFreightInvoiceRequestStatus.DRAFT -> {
                when (next) {
                    RollingFreightInvoiceRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingFreightInvoiceRequestStatus.PENDING -> {
                when (next) {
                    RollingFreightInvoiceRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingFreightInvoiceRequestStatus.ACTIVE -> return next == RollingFreightInvoiceRequestStatus.SUSPENDED || next == RollingFreightInvoiceRequestStatus.CLOSED
            RollingFreightInvoiceRequestStatus.SUSPENDED -> {
                when (next) {
                    RollingFreightInvoiceRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingFreightInvoiceRequestStatus) {
        if (!canMoveTo(next)) {
            error("rolling freight invoice request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((odometer.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RollingFreightInvoiceRequest {
        return RollingFreightInvoiceRequest(
            code = code,
            title = title,
            margin = margin,
            threshold = threshold,
            ownerId = ownerId,
            retries = retries,
            odometer = odometer,
            lane = lane,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling freight invoice request in status draft. */
fun newRollingFreightInvoiceRequest(code: String, title: String): RollingFreightInvoiceRequest {
    return RollingFreightInvoiceRequest(
        code = code,
        title = title,
        margin = 0.0,
        threshold = 0.0,
        ownerId = "",
        retries = 0,
        odometer = 0,
        lane = "",
        ratio = 0.0,
        status = RollingFreightInvoiceRequestStatus.DRAFT,
    )
}

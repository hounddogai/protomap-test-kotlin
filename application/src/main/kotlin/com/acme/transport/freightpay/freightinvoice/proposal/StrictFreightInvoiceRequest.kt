package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict freight invoice request is in its life cycle. */
enum class StrictFreightInvoiceRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict freight invoice request as the freight invoice team keeps it. */
data class StrictFreightInvoiceRequest(
    var code: String,
    var name: String,
    var unit: String,
    var graceDays: Int,
    var phone: String,
    var score: Double,
    var expiresAt: Int,
    var locale: String,
    var status: StrictFreightInvoiceRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFreightInvoiceRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFreightInvoiceRequestStatus): Boolean {
        when (status) {
            StrictFreightInvoiceRequestStatus.DRAFT -> {
                when (next) {
                    StrictFreightInvoiceRequestStatus.PENDING, StrictFreightInvoiceRequestStatus.ACTIVE -> return true
                }
                return false
            }
            StrictFreightInvoiceRequestStatus.PENDING -> {
                when (next) {
                    StrictFreightInvoiceRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictFreightInvoiceRequestStatus.ACTIVE -> {
                when (next) {
                    StrictFreightInvoiceRequestStatus.SUSPENDED, StrictFreightInvoiceRequestStatus.CLOSED -> return true
                }
                return false
            }
            StrictFreightInvoiceRequestStatus.SUSPENDED -> return next == StrictFreightInvoiceRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictFreightInvoiceRequestStatus) {
        if (!canMoveTo(next)) {
            error("strict freight invoice request $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round((expiresAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): StrictFreightInvoiceRequest {
        return StrictFreightInvoiceRequest(
            code = code,
            name = name,
            unit = unit,
            graceDays = graceDays,
            phone = phone,
            score = score,
            expiresAt = expiresAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict freight invoice request in status draft. */
fun newStrictFreightInvoiceRequest(code: String, name: String): StrictFreightInvoiceRequest {
    return StrictFreightInvoiceRequest(
        code = code,
        name = name,
        unit = "",
        graceDays = 0,
        phone = "",
        score = 0.0,
        expiresAt = 0,
        locale = "",
        status = StrictFreightInvoiceRequestStatus.DRAFT,
    )
}

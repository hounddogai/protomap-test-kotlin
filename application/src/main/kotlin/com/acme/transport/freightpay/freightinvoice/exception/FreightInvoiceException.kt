package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice exception records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice exception is in its life cycle. */
enum class FreightInvoiceExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One freight invoice exception, with what the planners need to act on it. */
data class FreightInvoiceException(
    var code: String,
    var title: String,
    var cost: Double,
    var ceiling: Double,
    var capacity: Int,
    var lane: String,
    var region: String,
    var description: String,
    var status: FreightInvoiceExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceExceptionStatus): Boolean {
        when (status) {
            FreightInvoiceExceptionStatus.NEW -> {
                when (next) {
                    FreightInvoiceExceptionStatus.QUEUED -> return true
                }
                return false
            }
            FreightInvoiceExceptionStatus.QUEUED -> return next == FreightInvoiceExceptionStatus.RUNNING
            FreightInvoiceExceptionStatus.RUNNING -> return next == FreightInvoiceExceptionStatus.DONE || next == FreightInvoiceExceptionStatus.FAILED
            FreightInvoiceExceptionStatus.DONE -> return next == FreightInvoiceExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceExceptionStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(capacity.toDouble() * cost * 100) / 100

    fun snapshot(): FreightInvoiceException {
        return FreightInvoiceException(
            code = code,
            title = title,
            cost = cost,
            ceiling = ceiling,
            capacity = capacity,
            lane = lane,
            region = region,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a freight invoice exception in status new. */
fun newFreightInvoiceException(code: String, title: String): FreightInvoiceException {
    return FreightInvoiceException(
        code = code,
        title = title,
        cost = 0.0,
        ceiling = 0.0,
        capacity = 0,
        lane = "",
        region = "",
        description = "",
        status = FreightInvoiceExceptionStatus.NEW,
    )
}

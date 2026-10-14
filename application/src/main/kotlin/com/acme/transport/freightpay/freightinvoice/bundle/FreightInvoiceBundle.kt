package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice bundle records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice bundle is in its life cycle. */
enum class FreightInvoiceBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a freight invoice bundle and its current status. */
data class FreightInvoiceBundle(
    var code: String,
    var name: String,
    var trailerId: String,
    var startsAt: Int,
    var depot: String,
    var shiftCode: String,
    var source: String,
    var driverId: String,
    var quantity: Int,
    var status: FreightInvoiceBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceBundleStatus): Boolean {
        when (status) {
            FreightInvoiceBundleStatus.NEW -> {
                when (next) {
                    FreightInvoiceBundleStatus.DONE, FreightInvoiceBundleStatus.FAILED -> return true
                }
                return false
            }
            FreightInvoiceBundleStatus.QUEUED -> {
                when (next) {
                    FreightInvoiceBundleStatus.RUNNING -> return true
                }
                return false
            }
            FreightInvoiceBundleStatus.RUNNING -> return next == FreightInvoiceBundleStatus.FAILED
            FreightInvoiceBundleStatus.DONE -> return next == FreightInvoiceBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceBundleStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"freight invoice bundle"} $code"

    fun weightedExposure(): Double = round(startsAt.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceBundle {
        return FreightInvoiceBundle(
            code = code,
            name = name,
            trailerId = trailerId,
            startsAt = startsAt,
            depot = depot,
            shiftCode = shiftCode,
            source = source,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a freight invoice bundle in status new. */
fun newFreightInvoiceBundle(code: String, name: String): FreightInvoiceBundle {
    return FreightInvoiceBundle(
        code = code,
        name = name,
        trailerId = "",
        startsAt = 0,
        depot = "",
        shiftCode = "",
        source = "",
        driverId = "",
        quantity = 0,
        status = FreightInvoiceBundleStatus.NEW,
    )
}

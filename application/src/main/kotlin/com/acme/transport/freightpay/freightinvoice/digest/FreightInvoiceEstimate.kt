package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice estimate is in its life cycle. */
enum class FreightInvoiceEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A freight invoice estimate as the freight invoice team keeps it. */
data class FreightInvoiceEstimate(
    var code: String,
    var name: String,
    var amount: Double,
    var dock: String,
    var version: Int,
    var externalRef: String,
    var locale: String,
    var carrier: String,
    var status: FreightInvoiceEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceEstimateStatus): Boolean {
        when (status) {
            FreightInvoiceEstimateStatus.REQUESTED -> return next == FreightInvoiceEstimateStatus.REVOKED
            FreightInvoiceEstimateStatus.GRANTED -> return next == FreightInvoiceEstimateStatus.REVOKED || next == FreightInvoiceEstimateStatus.EXPIRED
            FreightInvoiceEstimateStatus.REVOKED -> {
                when (next) {
                    FreightInvoiceEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceEstimateStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice estimate $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(amount * version.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceEstimate {
        return FreightInvoiceEstimate(
            code = code,
            name = name,
            amount = amount,
            dock = dock,
            version = version,
            externalRef = externalRef,
            locale = locale,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a freight invoice estimate in status requested. */
fun newFreightInvoiceEstimate(code: String, name: String): FreightInvoiceEstimate {
    return FreightInvoiceEstimate(
        code = code,
        name = name,
        amount = 0.0,
        dock = "",
        version = 0,
        externalRef = "",
        locale = "",
        carrier = "",
        status = FreightInvoiceEstimateStatus.REQUESTED,
    )
}

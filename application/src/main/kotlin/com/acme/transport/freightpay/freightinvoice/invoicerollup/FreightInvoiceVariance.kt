package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.kit.text.normalize

/** Where a freight invoice variance is in its life cycle. */
enum class FreightInvoiceVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A freight invoice variance as the freight invoice team keeps it. */
data class FreightInvoiceVariance(
    var code: String,
    var label: String,
    var active: Boolean,
    var country: String,
    var ownerId: String,
    var amount: Double,
    var status: FreightInvoiceVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceVarianceStatus): Boolean {
        when (status) {
            FreightInvoiceVarianceStatus.NEW -> {
                when (next) {
                    FreightInvoiceVarianceStatus.DONE, FreightInvoiceVarianceStatus.FAILED -> return true
                }
                return false
            }
            FreightInvoiceVarianceStatus.QUEUED -> {
                when (next) {
                    FreightInvoiceVarianceStatus.DONE -> return true
                }
                return false
            }
            FreightInvoiceVarianceStatus.RUNNING -> {
                when (next) {
                    FreightInvoiceVarianceStatus.DONE, FreightInvoiceVarianceStatus.FAILED -> return true
                }
                return false
            }
            FreightInvoiceVarianceStatus.DONE -> return next == FreightInvoiceVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceVarianceStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = amount

    fun snapshot(): FreightInvoiceVariance {
        return FreightInvoiceVariance(
            code = code,
            label = label,
            active = active,
            country = country,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a freight invoice variance in status new. */
fun newFreightInvoiceVariance(code: String, label: String): FreightInvoiceVariance {
    return FreightInvoiceVariance(
        code = code,
        label = label,
        active = false,
        country = "",
        ownerId = "",
        amount = 0.0,
        status = FreightInvoiceVarianceStatus.NEW,
    )
}

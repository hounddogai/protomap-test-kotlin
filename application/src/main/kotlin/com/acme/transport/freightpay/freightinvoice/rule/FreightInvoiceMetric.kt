package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice metric is in its life cycle. */
enum class FreightInvoiceMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A freight invoice metric. Its code is unique within a tenant. */
data class FreightInvoiceMetric(
    var code: String,
    var name: String,
    var pallets: Int,
    var rate: Double,
    var minimum: Double,
    var contactEmail: String,
    var lane: String,
    var status: FreightInvoiceMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceMetricStatus): Boolean {
        when (status) {
            FreightInvoiceMetricStatus.PROPOSED -> {
                when (next) {
                    FreightInvoiceMetricStatus.APPROVED, FreightInvoiceMetricStatus.REJECTED -> return true
                }
                return false
            }
            FreightInvoiceMetricStatus.APPROVED -> {
                when (next) {
                    FreightInvoiceMetricStatus.REJECTED, FreightInvoiceMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            FreightInvoiceMetricStatus.REJECTED -> return next == FreightInvoiceMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FreightInvoiceMetricStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"freight invoice metric"} $code"

    fun weightedVolume(): Double = round(rate * pallets.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceMetric {
        return FreightInvoiceMetric(
            code = code,
            name = name,
            pallets = pallets,
            rate = rate,
            minimum = minimum,
            contactEmail = contactEmail,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a freight invoice metric in status proposed. */
fun newFreightInvoiceMetric(code: String, name: String): FreightInvoiceMetric {
    return FreightInvoiceMetric(
        code = code,
        name = name,
        pallets = 0,
        rate = 0.0,
        minimum = 0.0,
        contactEmail = "",
        lane = "",
        status = FreightInvoiceMetricStatus.PROPOSED,
    )
}

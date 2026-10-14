package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.kit.text.normalize

/** Where a freight invoice digest is in its life cycle. */
enum class FreightInvoiceDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One freight invoice digest, with what the planners need to act on it. */
data class FreightInvoiceDigest(
    var code: String,
    var label: String,
    var routeCode: String,
    var ratio: Double,
    var name: String,
    var segment: String,
    var depot: String,
    var status: FreightInvoiceDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceDigestStatus): Boolean {
        when (status) {
            FreightInvoiceDigestStatus.PLANNED -> {
                when (next) {
                    FreightInvoiceDigestStatus.SCHEDULED -> return true
                }
                return false
            }
            FreightInvoiceDigestStatus.SCHEDULED -> {
                when (next) {
                    FreightInvoiceDigestStatus.IN_PROGRESS, FreightInvoiceDigestStatus.CANCELLED -> return true
                }
                return false
            }
            FreightInvoiceDigestStatus.IN_PROGRESS -> return next == FreightInvoiceDigestStatus.COMPLETED || next == FreightInvoiceDigestStatus.CANCELLED
            FreightInvoiceDigestStatus.COMPLETED -> {
                when (next) {
                    FreightInvoiceDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceDigestStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = ratio

    fun snapshot(): FreightInvoiceDigest {
        return FreightInvoiceDigest(
            code = code,
            label = label,
            routeCode = routeCode,
            ratio = ratio,
            name = name,
            segment = segment,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a freight invoice digest in status planned. */
fun newFreightInvoiceDigest(code: String, label: String): FreightInvoiceDigest {
    return FreightInvoiceDigest(
        code = code,
        label = label,
        routeCode = "",
        ratio = 0.0,
        name = "",
        segment = "",
        depot = "",
        status = FreightInvoiceDigestStatus.PLANNED,
    )
}

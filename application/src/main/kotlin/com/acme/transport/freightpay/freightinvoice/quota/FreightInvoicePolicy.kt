package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice policy is in its life cycle. */
enum class FreightInvoicePolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A freight invoice policy as the freight invoice team keeps it. */
data class FreightInvoicePolicy(
    var code: String,
    var label: String,
    var createdAt: Int,
    var loadKg: Double,
    var timezone: String,
    var status: FreightInvoicePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoicePolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoicePolicyStatus): Boolean {
        when (status) {
            FreightInvoicePolicyStatus.REQUESTED -> {
                when (next) {
                    FreightInvoicePolicyStatus.GRANTED, FreightInvoicePolicyStatus.REVOKED -> return true
                }
                return false
            }
            FreightInvoicePolicyStatus.GRANTED -> return next == FreightInvoicePolicyStatus.REVOKED || next == FreightInvoicePolicyStatus.EXPIRED
            FreightInvoicePolicyStatus.REVOKED -> return next == FreightInvoicePolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FreightInvoicePolicyStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round(loadKg * createdAt.toDouble() * 100) / 100

    fun snapshot(): FreightInvoicePolicy {
        return FreightInvoicePolicy(
            code = code,
            label = label,
            createdAt = createdAt,
            loadKg = loadKg,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a freight invoice policy in status requested. */
fun newFreightInvoicePolicy(code: String, label: String): FreightInvoicePolicy {
    return FreightInvoicePolicy(
        code = code,
        label = label,
        createdAt = 0,
        loadKg = 0.0,
        timezone = "",
        status = FreightInvoicePolicyStatus.REQUESTED,
    )
}

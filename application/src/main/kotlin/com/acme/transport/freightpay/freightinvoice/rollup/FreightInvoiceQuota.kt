package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice quota is in its life cycle. */
enum class FreightInvoiceQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A freight invoice quota. Its code is unique within a tenant. */
data class FreightInvoiceQuota(
    var code: String,
    var label: String,
    var endsAt: Int,
    var approved: Boolean,
    var notes: String,
    var createdAt: Int,
    var segment: String,
    var unit: String,
    var externalRef: String,
    var routeCode: String,
    var minimum: Double,
    var status: FreightInvoiceQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceQuotaStatus): Boolean {
        when (status) {
            FreightInvoiceQuotaStatus.REQUESTED -> return next == FreightInvoiceQuotaStatus.GRANTED || next == FreightInvoiceQuotaStatus.REVOKED
            FreightInvoiceQuotaStatus.GRANTED -> return next == FreightInvoiceQuotaStatus.EXPIRED
            FreightInvoiceQuotaStatus.REVOKED -> return next == FreightInvoiceQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceQuotaStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round((createdAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): FreightInvoiceQuota {
        return FreightInvoiceQuota(
            code = code,
            label = label,
            endsAt = endsAt,
            approved = approved,
            notes = notes,
            createdAt = createdAt,
            segment = segment,
            unit = unit,
            externalRef = externalRef,
            routeCode = routeCode,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a freight invoice quota in status requested. */
fun newFreightInvoiceQuota(code: String, label: String): FreightInvoiceQuota {
    return FreightInvoiceQuota(
        code = code,
        label = label,
        endsAt = 0,
        approved = false,
        notes = "",
        createdAt = 0,
        segment = "",
        unit = "",
        externalRef = "",
        routeCode = "",
        minimum = 0.0,
        status = FreightInvoiceQuotaStatus.REQUESTED,
    )
}

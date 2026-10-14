package com.acme.transport.freightpay.freightinvoice.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional freight invoice threshold records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a regional freight invoice threshold is in its life cycle. */
enum class RegionalFreightInvoiceThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional freight invoice threshold and its current status. */
data class RegionalFreightInvoiceThreshold(
    var code: String,
    var title: String,
    var label: String,
    var weight: Double,
    var maximum: Double,
    var loadKg: Double,
    var status: RegionalFreightInvoiceThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFreightInvoiceThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFreightInvoiceThresholdStatus): Boolean {
        when (status) {
            RegionalFreightInvoiceThresholdStatus.REQUESTED -> {
                when (next) {
                    RegionalFreightInvoiceThresholdStatus.REVOKED, RegionalFreightInvoiceThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalFreightInvoiceThresholdStatus.GRANTED -> {
                when (next) {
                    RegionalFreightInvoiceThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalFreightInvoiceThresholdStatus.REVOKED -> {
                when (next) {
                    RegionalFreightInvoiceThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFreightInvoiceThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional freight invoice threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun weightedTotal(): Double = round((loadKg + maximum) * 100) / 100

    fun snapshot(): RegionalFreightInvoiceThreshold {
        return RegionalFreightInvoiceThreshold(
            code = code,
            title = title,
            label = label,
            weight = weight,
            maximum = maximum,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional freight invoice threshold in status requested. */
fun newRegionalFreightInvoiceThreshold(code: String, title: String): RegionalFreightInvoiceThreshold {
    return RegionalFreightInvoiceThreshold(
        code = code,
        title = title,
        label = "",
        weight = 0.0,
        maximum = 0.0,
        loadKg = 0.0,
        status = RegionalFreightInvoiceThresholdStatus.REQUESTED,
    )
}

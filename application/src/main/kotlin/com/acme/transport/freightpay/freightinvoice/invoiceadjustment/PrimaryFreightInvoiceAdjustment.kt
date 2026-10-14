package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary freight invoice adjustment records of freightpay's freight invoice work, and the rules, stores,
// and calculations around them.

/** Where a primary freight invoice adjustment is in its life cycle. */
enum class PrimaryFreightInvoiceAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary freight invoice adjustment as the freight invoice team keeps it. */
data class PrimaryFreightInvoiceAdjustment(
    var code: String,
    var label: String,
    var currency: String,
    var title: String,
    var timezone: String,
    var odometer: Int,
    var stopCount: Int,
    var status: PrimaryFreightInvoiceAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFreightInvoiceAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFreightInvoiceAdjustmentStatus): Boolean {
        when (status) {
            PrimaryFreightInvoiceAdjustmentStatus.REQUESTED -> return next == PrimaryFreightInvoiceAdjustmentStatus.EXPIRED
            PrimaryFreightInvoiceAdjustmentStatus.GRANTED -> {
                when (next) {
                    PrimaryFreightInvoiceAdjustmentStatus.REVOKED, PrimaryFreightInvoiceAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryFreightInvoiceAdjustmentStatus.REVOKED -> {
                when (next) {
                    PrimaryFreightInvoiceAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryFreightInvoiceAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("primary freight invoice adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary freight invoice adjustment"} $code"

    fun effectiveVolume(): Double = round((odometer.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): PrimaryFreightInvoiceAdjustment {
        return PrimaryFreightInvoiceAdjustment(
            code = code,
            label = label,
            currency = currency,
            title = title,
            timezone = timezone,
            odometer = odometer,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary freight invoice adjustment in status requested. */
fun newPrimaryFreightInvoiceAdjustment(code: String, label: String): PrimaryFreightInvoiceAdjustment {
    return PrimaryFreightInvoiceAdjustment(
        code = code,
        label = label,
        currency = "",
        title = "",
        timezone = "",
        odometer = 0,
        stopCount = 0,
        status = PrimaryFreightInvoiceAdjustmentStatus.REQUESTED,
    )
}

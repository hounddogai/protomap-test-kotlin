package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual freight invoice window is in its life cycle. */
enum class ManualFreightInvoiceWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual freight invoice window. Its code is unique within a tenant. */
data class ManualFreightInvoiceWindow(
    var code: String,
    var label: String,
    var summary: String,
    var odometer: Int,
    var batchSize: Int,
    var pallets: Int,
    var endsAt: Int,
    var timezone: String,
    var ownerId: String,
    var minimum: Double,
    var notes: String,
    var status: ManualFreightInvoiceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFreightInvoiceWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFreightInvoiceWindowStatus): Boolean {
        when (status) {
            ManualFreightInvoiceWindowStatus.CREATED -> {
                when (next) {
                    ManualFreightInvoiceWindowStatus.VALIDATED -> return true
                }
                return false
            }
            ManualFreightInvoiceWindowStatus.VALIDATED -> {
                when (next) {
                    ManualFreightInvoiceWindowStatus.PUBLISHED, ManualFreightInvoiceWindowStatus.RETIRED -> return true
                }
                return false
            }
            ManualFreightInvoiceWindowStatus.PUBLISHED -> return next == ManualFreightInvoiceWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualFreightInvoiceWindowStatus) {
        if (!canMoveTo(next)) {
            error("manual freight invoice window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual freight invoice window"} $code"

    fun adjustedValue(): Double = round(odometer.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): ManualFreightInvoiceWindow {
        return ManualFreightInvoiceWindow(
            code = code,
            label = label,
            summary = summary,
            odometer = odometer,
            batchSize = batchSize,
            pallets = pallets,
            endsAt = endsAt,
            timezone = timezone,
            ownerId = ownerId,
            minimum = minimum,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual freight invoice window in status created. */
fun newManualFreightInvoiceWindow(code: String, label: String): ManualFreightInvoiceWindow {
    return ManualFreightInvoiceWindow(
        code = code,
        label = label,
        summary = "",
        odometer = 0,
        batchSize = 0,
        pallets = 0,
        endsAt = 0,
        timezone = "",
        ownerId = "",
        minimum = 0.0,
        notes = "",
        status = ManualFreightInvoiceWindowStatus.CREATED,
    )
}

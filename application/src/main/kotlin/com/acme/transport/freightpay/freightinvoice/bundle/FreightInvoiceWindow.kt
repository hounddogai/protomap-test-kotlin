package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice window is in its life cycle. */
enum class FreightInvoiceWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A freight invoice window. Its code is unique within a tenant. */
data class FreightInvoiceWindow(
    var code: String,
    var label: String,
    var channel: String,
    var carrier: String,
    var amount: Double,
    var fuelLiters: Double,
    var vehicleId: String,
    var margin: Double,
    var status: FreightInvoiceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceWindowStatus): Boolean {
        when (status) {
            FreightInvoiceWindowStatus.PLANNED -> return next == FreightInvoiceWindowStatus.IN_PROGRESS || next == FreightInvoiceWindowStatus.CANCELLED
            FreightInvoiceWindowStatus.SCHEDULED -> return next == FreightInvoiceWindowStatus.CANCELLED
            FreightInvoiceWindowStatus.IN_PROGRESS -> return next == FreightInvoiceWindowStatus.COMPLETED
            FreightInvoiceWindowStatus.COMPLETED -> {
                when (next) {
                    FreightInvoiceWindowStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceWindowStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"freight invoice window"} $code"

    fun effectiveValue(): Double = round((fuelLiters - margin) * 100) / 100

    fun snapshot(): FreightInvoiceWindow {
        return FreightInvoiceWindow(
            code = code,
            label = label,
            channel = channel,
            carrier = carrier,
            amount = amount,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a freight invoice window in status planned. */
fun newFreightInvoiceWindow(code: String, label: String): FreightInvoiceWindow {
    return FreightInvoiceWindow(
        code = code,
        label = label,
        channel = "",
        carrier = "",
        amount = 0.0,
        fuelLiters = 0.0,
        vehicleId = "",
        margin = 0.0,
        status = FreightInvoiceWindowStatus.PLANNED,
    )
}

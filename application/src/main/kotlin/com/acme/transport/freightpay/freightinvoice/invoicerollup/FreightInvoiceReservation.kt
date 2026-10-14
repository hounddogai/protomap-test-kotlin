package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.kit.text.normalize

/** Where a freight invoice reservation is in its life cycle. */
enum class FreightInvoiceReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A freight invoice reservation as the freight invoice team keeps it. */
data class FreightInvoiceReservation(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var active: Boolean,
    var distanceKm: Double,
    var status: FreightInvoiceReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceReservationStatus): Boolean {
        when (status) {
            FreightInvoiceReservationStatus.PLANNED -> return next == FreightInvoiceReservationStatus.CANCELLED
            FreightInvoiceReservationStatus.SCHEDULED -> return next == FreightInvoiceReservationStatus.COMPLETED
            FreightInvoiceReservationStatus.IN_PROGRESS -> return next == FreightInvoiceReservationStatus.COMPLETED || next == FreightInvoiceReservationStatus.CANCELLED
            FreightInvoiceReservationStatus.COMPLETED -> {
                when (next) {
                    FreightInvoiceReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceReservationStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): FreightInvoiceReservation {
        return FreightInvoiceReservation(
            code = code,
            name = name,
            pinned = pinned,
            active = active,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a freight invoice reservation in status planned. */
fun newFreightInvoiceReservation(code: String, name: String): FreightInvoiceReservation {
    return FreightInvoiceReservation(
        code = code,
        name = name,
        pinned = false,
        active = false,
        distanceKm = 0.0,
        status = FreightInvoiceReservationStatus.PLANNED,
    )
}

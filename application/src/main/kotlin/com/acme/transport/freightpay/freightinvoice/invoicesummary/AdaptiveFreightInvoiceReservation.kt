package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive freight invoice reservation is in its life cycle. */
enum class AdaptiveFreightInvoiceReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive freight invoice reservation and its current status. */
data class AdaptiveFreightInvoiceReservation(
    var code: String,
    var label: String,
    var amount: Double,
    var rate: Double,
    var unit: String,
    var ownerId: String,
    var margin: Double,
    var attempts: Int,
    var country: String,
    var distanceKm: Double,
    var depot: String,
    var status: AdaptiveFreightInvoiceReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveFreightInvoiceReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveFreightInvoiceReservationStatus): Boolean {
        when (status) {
            AdaptiveFreightInvoiceReservationStatus.PLANNED -> return next == AdaptiveFreightInvoiceReservationStatus.SCHEDULED || next == AdaptiveFreightInvoiceReservationStatus.CANCELLED
            AdaptiveFreightInvoiceReservationStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveFreightInvoiceReservationStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceReservationStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveFreightInvoiceReservationStatus.COMPLETED, AdaptiveFreightInvoiceReservationStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveFreightInvoiceReservationStatus.COMPLETED -> return next == AdaptiveFreightInvoiceReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveFreightInvoiceReservationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive freight invoice reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive freight invoice reservation"} $code"

    fun projectedExposure(): Double = round(rate * attempts.toDouble() * 100) / 100

    fun snapshot(): AdaptiveFreightInvoiceReservation {
        return AdaptiveFreightInvoiceReservation(
            code = code,
            label = label,
            amount = amount,
            rate = rate,
            unit = unit,
            ownerId = ownerId,
            margin = margin,
            attempts = attempts,
            country = country,
            distanceKm = distanceKm,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive freight invoice reservation in status planned. */
fun newAdaptiveFreightInvoiceReservation(code: String, label: String): AdaptiveFreightInvoiceReservation {
    return AdaptiveFreightInvoiceReservation(
        code = code,
        label = label,
        amount = 0.0,
        rate = 0.0,
        unit = "",
        ownerId = "",
        margin = 0.0,
        attempts = 0,
        country = "",
        distanceKm = 0.0,
        depot = "",
        status = AdaptiveFreightInvoiceReservationStatus.PLANNED,
    )
}

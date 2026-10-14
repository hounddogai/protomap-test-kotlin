package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge reservation is in its life cycle. */
enum class FuelSurchargeReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fuel surcharge reservation. Its code is unique within a tenant. */
data class FuelSurchargeReservation(
    var code: String,
    var name: String,
    var dock: String,
    var currency: String,
    var attempts: Int,
    var status: FuelSurchargeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeReservationStatus): Boolean {
        when (status) {
            FuelSurchargeReservationStatus.PLANNED -> {
                when (next) {
                    FuelSurchargeReservationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FuelSurchargeReservationStatus.SCHEDULED -> return next == FuelSurchargeReservationStatus.IN_PROGRESS || next == FuelSurchargeReservationStatus.CANCELLED
            FuelSurchargeReservationStatus.IN_PROGRESS -> return next == FuelSurchargeReservationStatus.CANCELLED
            FuelSurchargeReservationStatus.COMPLETED -> {
                when (next) {
                    FuelSurchargeReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeReservationStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel surcharge reservation"} $code"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): FuelSurchargeReservation {
        return FuelSurchargeReservation(
            code = code,
            name = name,
            dock = dock,
            currency = currency,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel surcharge reservation in status planned. */
fun newFuelSurchargeReservation(code: String, name: String): FuelSurchargeReservation {
    return FuelSurchargeReservation(
        code = code,
        name = name,
        dock = "",
        currency = "",
        attempts = 0,
        status = FuelSurchargeReservationStatus.PLANNED,
    )
}

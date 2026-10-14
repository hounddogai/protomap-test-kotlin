package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge reservation is in its life cycle. */
enum class EvChargeReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A ev charge reservation. Its code is unique within a tenant. */
data class EvChargeReservation(
    var code: String,
    var label: String,
    var createdAt: Int,
    var cost: Double,
    var country: String,
    var durationMinutes: Int,
    var discount: Double,
    var segment: String,
    var pallets: Int,
    var status: EvChargeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeReservationStatus): Boolean {
        when (status) {
            EvChargeReservationStatus.NEW -> return next == EvChargeReservationStatus.DONE
            EvChargeReservationStatus.QUEUED -> return next == EvChargeReservationStatus.DONE
            EvChargeReservationStatus.RUNNING -> return next == EvChargeReservationStatus.DONE
            EvChargeReservationStatus.DONE -> return next == EvChargeReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EvChargeReservationStatus) {
        if (!canMoveTo(next)) {
            error("ev charge reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round((durationMinutes.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): EvChargeReservation {
        return EvChargeReservation(
            code = code,
            label = label,
            createdAt = createdAt,
            cost = cost,
            country = country,
            durationMinutes = durationMinutes,
            discount = discount,
            segment = segment,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge reservation in status new. */
fun newEvChargeReservation(code: String, label: String): EvChargeReservation {
    return EvChargeReservation(
        code = code,
        label = label,
        createdAt = 0,
        cost = 0.0,
        country = "",
        durationMinutes = 0,
        discount = 0.0,
        segment = "",
        pallets = 0,
        status = EvChargeReservationStatus.NEW,
    )
}

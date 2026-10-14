package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled zone reservation is in its life cycle. */
enum class ScheduledZoneReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled zone reservation. Its code is unique within a tenant. */
data class ScheduledZoneReservation(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var carrier: String,
    var loadKg: Double,
    var retries: Int,
    var amount: Double,
    var maximum: Double,
    var odometer: Int,
    var currency: String,
    var status: ScheduledZoneReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledZoneReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledZoneReservationStatus): Boolean {
        when (status) {
            ScheduledZoneReservationStatus.OPEN -> return next == ScheduledZoneReservationStatus.ARCHIVED
            ScheduledZoneReservationStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledZoneReservationStatus.REOPENED, ScheduledZoneReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledZoneReservationStatus.RESOLVED -> return next == ScheduledZoneReservationStatus.ARCHIVED
            ScheduledZoneReservationStatus.REOPENED -> return next == ScheduledZoneReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledZoneReservationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled zone reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round((loadKg - odometer.toDouble()) * 100) / 100

    fun snapshot(): ScheduledZoneReservation {
        return ScheduledZoneReservation(
            code = code,
            label = label,
            pinned = pinned,
            carrier = carrier,
            loadKg = loadKg,
            retries = retries,
            amount = amount,
            maximum = maximum,
            odometer = odometer,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled zone reservation in status open. */
fun newScheduledZoneReservation(code: String, label: String): ScheduledZoneReservation {
    return ScheduledZoneReservation(
        code = code,
        label = label,
        pinned = false,
        carrier = "",
        loadKg = 0.0,
        retries = 0,
        amount = 0.0,
        maximum = 0.0,
        odometer = 0,
        currency = "",
        status = ScheduledZoneReservationStatus.OPEN,
    )
}

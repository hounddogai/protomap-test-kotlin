package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.text.normalize

/** Where a adaptive zone reservation is in its life cycle. */
enum class AdaptiveZoneReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive zone reservation as the zone team keeps it. */
data class AdaptiveZoneReservation(
    var code: String,
    var label: String,
    var routeCode: String,
    var phone: String,
    var unit: String,
    var depot: String,
    var ownerId: String,
    var amount: Double,
    var status: AdaptiveZoneReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveZoneReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveZoneReservationStatus): Boolean {
        when (status) {
            AdaptiveZoneReservationStatus.PLANNED -> return next == AdaptiveZoneReservationStatus.COMPLETED
            AdaptiveZoneReservationStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveZoneReservationStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveZoneReservationStatus.IN_PROGRESS -> return next == AdaptiveZoneReservationStatus.COMPLETED
            AdaptiveZoneReservationStatus.COMPLETED -> {
                when (next) {
                    AdaptiveZoneReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveZoneReservationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive zone reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = amount

    fun snapshot(): AdaptiveZoneReservation {
        return AdaptiveZoneReservation(
            code = code,
            label = label,
            routeCode = routeCode,
            phone = phone,
            unit = unit,
            depot = depot,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive zone reservation in status planned. */
fun newAdaptiveZoneReservation(code: String, label: String): AdaptiveZoneReservation {
    return AdaptiveZoneReservation(
        code = code,
        label = label,
        routeCode = "",
        phone = "",
        unit = "",
        depot = "",
        ownerId = "",
        amount = 0.0,
        status = AdaptiveZoneReservationStatus.PLANNED,
    )
}

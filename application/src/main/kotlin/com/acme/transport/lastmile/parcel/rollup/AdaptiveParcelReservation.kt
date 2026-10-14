package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive parcel reservation is in its life cycle. */
enum class AdaptiveParcelReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive parcel reservation. Its code is unique within a tenant. */
data class AdaptiveParcelReservation(
    var code: String,
    var name: String,
    var vehicleId: String,
    var floorValue: Double,
    var label: String,
    var ratio: Double,
    var cost: Double,
    var region: String,
    var status: AdaptiveParcelReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveParcelReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveParcelReservationStatus): Boolean {
        when (status) {
            AdaptiveParcelReservationStatus.DRAFT -> return next == AdaptiveParcelReservationStatus.ACTIVE || next == AdaptiveParcelReservationStatus.SUSPENDED
            AdaptiveParcelReservationStatus.PENDING -> return next == AdaptiveParcelReservationStatus.ACTIVE || next == AdaptiveParcelReservationStatus.CLOSED
            AdaptiveParcelReservationStatus.ACTIVE -> return next == AdaptiveParcelReservationStatus.SUSPENDED
            AdaptiveParcelReservationStatus.SUSPENDED -> return next == AdaptiveParcelReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveParcelReservationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive parcel reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun projectedTotal(): Double = round(floorValue * ratio * 100) / 100

    fun snapshot(): AdaptiveParcelReservation {
        return AdaptiveParcelReservation(
            code = code,
            name = name,
            vehicleId = vehicleId,
            floorValue = floorValue,
            label = label,
            ratio = ratio,
            cost = cost,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive parcel reservation in status draft. */
fun newAdaptiveParcelReservation(code: String, name: String): AdaptiveParcelReservation {
    return AdaptiveParcelReservation(
        code = code,
        name = name,
        vehicleId = "",
        floorValue = 0.0,
        label = "",
        ratio = 0.0,
        cost = 0.0,
        region = "",
        status = AdaptiveParcelReservationStatus.DRAFT,
    )
}

package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback camera event reservation is in its life cycle. */
enum class FallbackCameraEventReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fallback camera event reservation, with what the planners need to act on it. */
data class FallbackCameraEventReservation(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var quantity: Int,
    var source: String,
    var carrier: String,
    var batchSize: Int,
    var depot: String,
    var reviewer: String,
    var threshold: Double,
    var status: FallbackCameraEventReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCameraEventReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCameraEventReservationStatus): Boolean {
        when (status) {
            FallbackCameraEventReservationStatus.REQUESTED -> {
                when (next) {
                    FallbackCameraEventReservationStatus.GRANTED, FallbackCameraEventReservationStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackCameraEventReservationStatus.GRANTED -> return next == FallbackCameraEventReservationStatus.REVOKED || next == FallbackCameraEventReservationStatus.EXPIRED
            FallbackCameraEventReservationStatus.REVOKED -> {
                when (next) {
                    FallbackCameraEventReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCameraEventReservationStatus) {
        if (!canMoveTo(next)) {
            error("fallback camera event reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((quantity.toDouble() - threshold) * 100) / 100

    fun snapshot(): FallbackCameraEventReservation {
        return FallbackCameraEventReservation(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            quantity = quantity,
            source = source,
            carrier = carrier,
            batchSize = batchSize,
            depot = depot,
            reviewer = reviewer,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback camera event reservation in status requested. */
fun newFallbackCameraEventReservation(code: String, title: String): FallbackCameraEventReservation {
    return FallbackCameraEventReservation(
        code = code,
        title = title,
        fuelLiters = 0.0,
        quantity = 0,
        source = "",
        carrier = "",
        batchSize = 0,
        depot = "",
        reviewer = "",
        threshold = 0.0,
        status = FallbackCameraEventReservationStatus.REQUESTED,
    )
}

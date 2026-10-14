package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer reservation is in its life cycle. */
enum class TrailerReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a trailer reservation and its current status. */
data class TrailerReservation(
    var code: String,
    var name: String,
    var batchSize: Int,
    var revision: Int,
    var summary: String,
    var archived: Boolean,
    var fuelLiters: Double,
    var discount: Double,
    var maximum: Double,
    var carrier: String,
    var status: TrailerReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerReservationStatus): Boolean {
        when (status) {
            TrailerReservationStatus.PROPOSED -> {
                when (next) {
                    TrailerReservationStatus.APPROVED, TrailerReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerReservationStatus.APPROVED -> return next == TrailerReservationStatus.REJECTED || next == TrailerReservationStatus.WITHDRAWN
            TrailerReservationStatus.REJECTED -> return next == TrailerReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TrailerReservationStatus) {
        if (!canMoveTo(next)) {
            error("trailer reservation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"trailer reservation"} $code"

    fun projectedExposure(): Double = round(discount * maximum * 100) / 100

    fun snapshot(): TrailerReservation {
        return TrailerReservation(
            code = code,
            name = name,
            batchSize = batchSize,
            revision = revision,
            summary = summary,
            archived = archived,
            fuelLiters = fuelLiters,
            discount = discount,
            maximum = maximum,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer reservation in status proposed. */
fun newTrailerReservation(code: String, name: String): TrailerReservation {
    return TrailerReservation(
        code = code,
        name = name,
        batchSize = 0,
        revision = 0,
        summary = "",
        archived = false,
        fuelLiters = 0.0,
        discount = 0.0,
        maximum = 0.0,
        carrier = "",
        status = TrailerReservationStatus.PROPOSED,
    )
}

package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional service area reservation is in its life cycle. */
enum class RegionalServiceAreaReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a regional service area reservation and its current status. */
data class RegionalServiceAreaReservation(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var channel: String,
    var distanceKm: Double,
    var sequence: Int,
    var shiftCode: String,
    var startsAt: Int,
    var externalRef: String,
    var cost: Double,
    var status: RegionalServiceAreaReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaReservationStatus): Boolean {
        when (status) {
            RegionalServiceAreaReservationStatus.PLANNED -> return next == RegionalServiceAreaReservationStatus.IN_PROGRESS || next == RegionalServiceAreaReservationStatus.CANCELLED
            RegionalServiceAreaReservationStatus.SCHEDULED -> {
                when (next) {
                    RegionalServiceAreaReservationStatus.COMPLETED, RegionalServiceAreaReservationStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalServiceAreaReservationStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalServiceAreaReservationStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalServiceAreaReservationStatus.COMPLETED -> {
                when (next) {
                    RegionalServiceAreaReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaReservationStatus) {
        if (!canMoveTo(next)) {
            error("regional service area reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional service area reservation"} $code"

    fun adjustedLoad(): Double = round((distanceKm - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalServiceAreaReservation {
        return RegionalServiceAreaReservation(
            code = code,
            title = title,
            expiresAt = expiresAt,
            channel = channel,
            distanceKm = distanceKm,
            sequence = sequence,
            shiftCode = shiftCode,
            startsAt = startsAt,
            externalRef = externalRef,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional service area reservation in status planned. */
fun newRegionalServiceAreaReservation(code: String, title: String): RegionalServiceAreaReservation {
    return RegionalServiceAreaReservation(
        code = code,
        title = title,
        expiresAt = 0,
        channel = "",
        distanceKm = 0.0,
        sequence = 0,
        shiftCode = "",
        startsAt = 0,
        externalRef = "",
        cost = 0.0,
        status = RegionalServiceAreaReservationStatus.PLANNED,
    )
}

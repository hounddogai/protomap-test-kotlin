package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance reservation is in its life cycle. */
enum class ClearanceReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance reservation. Its code is unique within a tenant. */
data class ClearanceReservation(
    var code: String,
    var name: String,
    var notes: String,
    var startsAt: Int,
    var weight: Double,
    var windowDays: Int,
    var ratio: Double,
    var segment: String,
    var status: ClearanceReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceReservationStatus): Boolean {
        when (status) {
            ClearanceReservationStatus.IDLE -> {
                when (next) {
                    ClearanceReservationStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceReservationStatus.WARMING -> {
                when (next) {
                    ClearanceReservationStatus.READY, ClearanceReservationStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceReservationStatus.READY -> return next == ClearanceReservationStatus.DRAINING
            ClearanceReservationStatus.DRAINING -> return next == ClearanceReservationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ClearanceReservationStatus) {
        if (!canMoveTo(next)) {
            error("clearance reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round(windowDays.toDouble() * ratio * 100) / 100

    fun snapshot(): ClearanceReservation {
        return ClearanceReservation(
            code = code,
            name = name,
            notes = notes,
            startsAt = startsAt,
            weight = weight,
            windowDays = windowDays,
            ratio = ratio,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a clearance reservation in status idle. */
fun newClearanceReservation(code: String, name: String): ClearanceReservation {
    return ClearanceReservation(
        code = code,
        name = name,
        notes = "",
        startsAt = 0,
        weight = 0.0,
        windowDays = 0,
        ratio = 0.0,
        segment = "",
        status = ClearanceReservationStatus.IDLE,
    )
}

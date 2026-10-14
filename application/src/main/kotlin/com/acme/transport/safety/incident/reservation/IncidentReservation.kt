package com.acme.transport.safety.incident.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident reservation records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a incident reservation is in its life cycle. */
enum class IncidentReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One incident reservation, with what the planners need to act on it. */
data class IncidentReservation(
    var code: String,
    var title: String,
    var reviewer: String,
    var startsAt: Int,
    var stopCount: Int,
    var shiftCode: String,
    var category: String,
    var region: String,
    var routeCode: String,
    var pinned: Boolean,
    var segment: String,
    var status: IncidentReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentReservationStatus): Boolean {
        when (status) {
            IncidentReservationStatus.IDLE -> {
                when (next) {
                    IncidentReservationStatus.READY -> return true
                }
                return false
            }
            IncidentReservationStatus.WARMING -> return next == IncidentReservationStatus.DRAINING || next == IncidentReservationStatus.STOPPED
            IncidentReservationStatus.READY -> {
                when (next) {
                    IncidentReservationStatus.DRAINING, IncidentReservationStatus.STOPPED -> return true
                }
                return false
            }
            IncidentReservationStatus.DRAINING -> {
                when (next) {
                    IncidentReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentReservationStatus) {
        if (!canMoveTo(next)) {
            error("incident reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"incident reservation"} $code"

    fun projectedExposure(): Double = round((stopCount.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): IncidentReservation {
        return IncidentReservation(
            code = code,
            title = title,
            reviewer = reviewer,
            startsAt = startsAt,
            stopCount = stopCount,
            shiftCode = shiftCode,
            category = category,
            region = region,
            routeCode = routeCode,
            pinned = pinned,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a incident reservation in status idle. */
fun newIncidentReservation(code: String, title: String): IncidentReservation {
    return IncidentReservation(
        code = code,
        title = title,
        reviewer = "",
        startsAt = 0,
        stopCount = 0,
        shiftCode = "",
        category = "",
        region = "",
        routeCode = "",
        pinned = false,
        segment = "",
        status = IncidentReservationStatus.IDLE,
    )
}

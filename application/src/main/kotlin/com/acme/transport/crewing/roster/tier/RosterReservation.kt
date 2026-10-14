package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster reservation is in its life cycle. */
enum class RosterReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One roster reservation, with what the planners need to act on it. */
data class RosterReservation(
    var code: String,
    var title: String,
    var depot: String,
    var retries: Int,
    var stopCount: Int,
    var label: String,
    var channel: String,
    var threshold: Double,
    var limit: Int,
    var status: RosterReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterReservationStatus): Boolean {
        when (status) {
            RosterReservationStatus.OPEN -> return next == RosterReservationStatus.REOPENED
            RosterReservationStatus.IN_REVIEW -> return next == RosterReservationStatus.REOPENED || next == RosterReservationStatus.ARCHIVED
            RosterReservationStatus.RESOLVED -> return next == RosterReservationStatus.ARCHIVED
            RosterReservationStatus.REOPENED -> return next == RosterReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RosterReservationStatus) {
        if (!canMoveTo(next)) {
            error("roster reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun projectedLoad(): Double = round((threshold - retries.toDouble()) * 100) / 100

    fun snapshot(): RosterReservation {
        return RosterReservation(
            code = code,
            title = title,
            depot = depot,
            retries = retries,
            stopCount = stopCount,
            label = label,
            channel = channel,
            threshold = threshold,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a roster reservation in status open. */
fun newRosterReservation(code: String, title: String): RosterReservation {
    return RosterReservation(
        code = code,
        title = title,
        depot = "",
        retries = 0,
        stopCount = 0,
        label = "",
        channel = "",
        threshold = 0.0,
        limit = 0,
        status = RosterReservationStatus.OPEN,
    )
}

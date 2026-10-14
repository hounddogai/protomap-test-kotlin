package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break reservation is in its life cycle. */
enum class RestBreakReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rest break reservation and its current status. */
data class RestBreakReservation(
    var code: String,
    var title: String,
    var archived: Boolean,
    var description: String,
    var floorValue: Double,
    var ceiling: Double,
    var contactEmail: String,
    var sequence: Int,
    var dock: String,
    var ownerId: String,
    var status: RestBreakReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakReservationStatus): Boolean {
        when (status) {
            RestBreakReservationStatus.OPEN -> return next == RestBreakReservationStatus.IN_REVIEW
            RestBreakReservationStatus.IN_REVIEW -> {
                when (next) {
                    RestBreakReservationStatus.RESOLVED -> return true
                }
                return false
            }
            RestBreakReservationStatus.RESOLVED -> return next == RestBreakReservationStatus.REOPENED
            RestBreakReservationStatus.REOPENED -> return next == RestBreakReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RestBreakReservationStatus) {
        if (!canMoveTo(next)) {
            error("rest break reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rest break reservation"} $code"

    fun projectedLoad(): Double = round((sequence.toDouble() - floorValue) * 100) / 100

    fun snapshot(): RestBreakReservation {
        return RestBreakReservation(
            code = code,
            title = title,
            archived = archived,
            description = description,
            floorValue = floorValue,
            ceiling = ceiling,
            contactEmail = contactEmail,
            sequence = sequence,
            dock = dock,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rest break reservation in status open. */
fun newRestBreakReservation(code: String, title: String): RestBreakReservation {
    return RestBreakReservation(
        code = code,
        title = title,
        archived = false,
        description = "",
        floorValue = 0.0,
        ceiling = 0.0,
        contactEmail = "",
        sequence = 0,
        dock = "",
        ownerId = "",
        status = RestBreakReservationStatus.OPEN,
    )
}

package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection reservation records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection reservation is in its life cycle. */
enum class InspectionReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection reservation as the inspection team keeps it. */
data class InspectionReservation(
    var code: String,
    var name: String,
    var timezone: String,
    var locale: String,
    var ownerId: String,
    var attempts: Int,
    var source: String,
    var archived: Boolean,
    var weight: Double,
    var reviewer: String,
    var startsAt: Int,
    var status: InspectionReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionReservationStatus): Boolean {
        when (status) {
            InspectionReservationStatus.DRAFT -> {
                when (next) {
                    InspectionReservationStatus.SUSPENDED, InspectionReservationStatus.CLOSED -> return true
                }
                return false
            }
            InspectionReservationStatus.PENDING -> return next == InspectionReservationStatus.SUSPENDED || next == InspectionReservationStatus.CLOSED
            InspectionReservationStatus.ACTIVE -> {
                when (next) {
                    InspectionReservationStatus.SUSPENDED, InspectionReservationStatus.CLOSED -> return true
                }
                return false
            }
            InspectionReservationStatus.SUSPENDED -> return next == InspectionReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionReservationStatus) {
        if (!canMoveTo(next)) {
            error("inspection reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round(attempts.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): InspectionReservation {
        return InspectionReservation(
            code = code,
            name = name,
            timezone = timezone,
            locale = locale,
            ownerId = ownerId,
            attempts = attempts,
            source = source,
            archived = archived,
            weight = weight,
            reviewer = reviewer,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a inspection reservation in status draft. */
fun newInspectionReservation(code: String, name: String): InspectionReservation {
    return InspectionReservation(
        code = code,
        name = name,
        timezone = "",
        locale = "",
        ownerId = "",
        attempts = 0,
        source = "",
        archived = false,
        weight = 0.0,
        reviewer = "",
        startsAt = 0,
        status = InspectionReservationStatus.DRAFT,
    )
}

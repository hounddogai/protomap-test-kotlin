package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy incident reservation records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a legacy incident reservation is in its life cycle. */
enum class LegacyIncidentReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy incident reservation as the incident team keeps it. */
data class LegacyIncidentReservation(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var active: Boolean,
    var archived: Boolean,
    var notes: String,
    var graceDays: Int,
    var distanceKm: Double,
    var name: String,
    var ceiling: Double,
    var status: LegacyIncidentReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentReservationStatus): Boolean {
        when (status) {
            LegacyIncidentReservationStatus.CREATED -> {
                when (next) {
                    LegacyIncidentReservationStatus.VALIDATED, LegacyIncidentReservationStatus.RETIRED -> return true
                }
                return false
            }
            LegacyIncidentReservationStatus.VALIDATED -> {
                when (next) {
                    LegacyIncidentReservationStatus.PUBLISHED, LegacyIncidentReservationStatus.RETIRED -> return true
                }
                return false
            }
            LegacyIncidentReservationStatus.PUBLISHED -> return next == LegacyIncidentReservationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyIncidentReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((distanceKm - graceDays.toDouble()) * 100) / 100

    fun snapshot(): LegacyIncidentReservation {
        return LegacyIncidentReservation(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            active = active,
            archived = archived,
            notes = notes,
            graceDays = graceDays,
            distanceKm = distanceKm,
            name = name,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy incident reservation in status created. */
fun newLegacyIncidentReservation(code: String, title: String): LegacyIncidentReservation {
    return LegacyIncidentReservation(
        code = code,
        title = title,
        etaMinutes = 0,
        active = false,
        archived = false,
        notes = "",
        graceDays = 0,
        distanceKm = 0.0,
        name = "",
        ceiling = 0.0,
        status = LegacyIncidentReservationStatus.CREATED,
    )
}

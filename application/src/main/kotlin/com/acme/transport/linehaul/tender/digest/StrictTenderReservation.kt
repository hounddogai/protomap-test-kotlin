package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict tender reservation is in its life cycle. */
enum class StrictTenderReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict tender reservation. Its code is unique within a tenant. */
data class StrictTenderReservation(
    var code: String,
    var title: String,
    var lane: String,
    var weight: Double,
    var revision: Int,
    var loadKg: Double,
    var discount: Double,
    var category: String,
    var floorValue: Double,
    var notes: String,
    var status: StrictTenderReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTenderReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTenderReservationStatus): Boolean {
        when (status) {
            StrictTenderReservationStatus.PLANNED -> return next == StrictTenderReservationStatus.SCHEDULED || next == StrictTenderReservationStatus.CANCELLED
            StrictTenderReservationStatus.SCHEDULED -> return next == StrictTenderReservationStatus.IN_PROGRESS || next == StrictTenderReservationStatus.COMPLETED
            StrictTenderReservationStatus.IN_PROGRESS -> {
                when (next) {
                    StrictTenderReservationStatus.CANCELLED -> return true
                }
                return false
            }
            StrictTenderReservationStatus.COMPLETED -> return next == StrictTenderReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictTenderReservationStatus) {
        if (!canMoveTo(next)) {
            error("strict tender reservation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((revision.toDouble() + loadKg) * 100) / 100

    fun snapshot(): StrictTenderReservation {
        return StrictTenderReservation(
            code = code,
            title = title,
            lane = lane,
            weight = weight,
            revision = revision,
            loadKg = loadKg,
            discount = discount,
            category = category,
            floorValue = floorValue,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict tender reservation in status planned. */
fun newStrictTenderReservation(code: String, title: String): StrictTenderReservation {
    return StrictTenderReservation(
        code = code,
        title = title,
        lane = "",
        weight = 0.0,
        revision = 0,
        loadKg = 0.0,
        discount = 0.0,
        category = "",
        floorValue = 0.0,
        notes = "",
        status = StrictTenderReservationStatus.PLANNED,
    )
}

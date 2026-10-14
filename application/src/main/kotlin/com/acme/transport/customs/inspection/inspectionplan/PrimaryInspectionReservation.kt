package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary inspection reservation is in its life cycle. */
enum class PrimaryInspectionReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary inspection reservation as the inspection team keeps it. */
data class PrimaryInspectionReservation(
    var code: String,
    var label: String,
    var checksum: String,
    var carrier: String,
    var startsAt: Int,
    var limit: Int,
    var score: Double,
    var notes: String,
    var status: PrimaryInspectionReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionReservationStatus): Boolean {
        when (status) {
            PrimaryInspectionReservationStatus.NEW -> {
                when (next) {
                    PrimaryInspectionReservationStatus.DONE, PrimaryInspectionReservationStatus.FAILED -> return true
                }
                return false
            }
            PrimaryInspectionReservationStatus.QUEUED -> {
                when (next) {
                    PrimaryInspectionReservationStatus.RUNNING -> return true
                }
                return false
            }
            PrimaryInspectionReservationStatus.RUNNING -> return next == PrimaryInspectionReservationStatus.DONE
            PrimaryInspectionReservationStatus.DONE -> return next == PrimaryInspectionReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionReservationStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((score + startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryInspectionReservation {
        return PrimaryInspectionReservation(
            code = code,
            label = label,
            checksum = checksum,
            carrier = carrier,
            startsAt = startsAt,
            limit = limit,
            score = score,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary inspection reservation in status new. */
fun newPrimaryInspectionReservation(code: String, label: String): PrimaryInspectionReservation {
    return PrimaryInspectionReservation(
        code = code,
        label = label,
        checksum = "",
        carrier = "",
        startsAt = 0,
        limit = 0,
        score = 0.0,
        notes = "",
        status = PrimaryInspectionReservationStatus.NEW,
    )
}

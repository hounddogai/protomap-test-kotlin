package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.normalize

/** Where a inspection reservation is in its life cycle. */
enum class InspectionReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection reservation as the inspection team keeps it. */
data class InspectionReservation(
    var code: String,
    var label: String,
    var retries: Int,
    var ownerId: String,
    var active: Boolean,
    var shiftCode: String,
    var dock: String,
    var status: InspectionReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionReservationStatus): Boolean {
        when (status) {
            InspectionReservationStatus.PLANNED -> {
                when (next) {
                    InspectionReservationStatus.SCHEDULED, InspectionReservationStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionReservationStatus.SCHEDULED -> {
                when (next) {
                    InspectionReservationStatus.COMPLETED, InspectionReservationStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionReservationStatus.IN_PROGRESS -> return next == InspectionReservationStatus.COMPLETED || next == InspectionReservationStatus.CANCELLED
            InspectionReservationStatus.COMPLETED -> return next == InspectionReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionReservationStatus) {
        if (!canMoveTo(next)) {
            error("inspection reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection reservation"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): InspectionReservation {
        return InspectionReservation(
            code = code,
            label = label,
            retries = retries,
            ownerId = ownerId,
            active = active,
            shiftCode = shiftCode,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection reservation in status planned. */
fun newInspectionReservation(code: String, label: String): InspectionReservation {
    return InspectionReservation(
        code = code,
        label = label,
        retries = 0,
        ownerId = "",
        active = false,
        shiftCode = "",
        dock = "",
        status = InspectionReservationStatus.PLANNED,
    )
}

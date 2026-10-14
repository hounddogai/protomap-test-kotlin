package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.normalize

/** Where a capped zone reservation is in its life cycle. */
enum class CappedZoneReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped zone reservation and its current status. */
data class CappedZoneReservation(
    var code: String,
    var label: String,
    var notes: String,
    var locale: String,
    var shiftCode: String,
    var durationMinutes: Int,
    var status: CappedZoneReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedZoneReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedZoneReservationStatus): Boolean {
        when (status) {
            CappedZoneReservationStatus.DRAFT -> return next == CappedZoneReservationStatus.SUSPENDED
            CappedZoneReservationStatus.PENDING -> {
                when (next) {
                    CappedZoneReservationStatus.SUSPENDED, CappedZoneReservationStatus.CLOSED -> return true
                }
                return false
            }
            CappedZoneReservationStatus.ACTIVE -> {
                when (next) {
                    CappedZoneReservationStatus.SUSPENDED, CappedZoneReservationStatus.CLOSED -> return true
                }
                return false
            }
            CappedZoneReservationStatus.SUSPENDED -> return next == CappedZoneReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedZoneReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped zone reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): CappedZoneReservation {
        return CappedZoneReservation(
            code = code,
            label = label,
            notes = notes,
            locale = locale,
            shiftCode = shiftCode,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped zone reservation in status draft. */
fun newCappedZoneReservation(code: String, label: String): CappedZoneReservation {
    return CappedZoneReservation(
        code = code,
        label = label,
        notes = "",
        locale = "",
        shiftCode = "",
        durationMinutes = 0,
        status = CappedZoneReservationStatus.DRAFT,
    )
}

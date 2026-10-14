package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The camera event reservation records of safety's camera event work, and the rules, stores, and calculations
// around them.

/** Where a camera event reservation is in its life cycle. */
enum class CameraEventReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One camera event reservation, with what the planners need to act on it. */
data class CameraEventReservation(
    var code: String,
    var title: String,
    var batchSize: Int,
    var reviewer: String,
    var durationMinutes: Int,
    var locale: String,
    var cost: Double,
    var ratio: Double,
    var endsAt: Int,
    var pallets: Int,
    var notes: String,
    var status: CameraEventReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventReservationStatus): Boolean {
        when (status) {
            CameraEventReservationStatus.REQUESTED -> return next == CameraEventReservationStatus.GRANTED
            CameraEventReservationStatus.GRANTED -> return next == CameraEventReservationStatus.REVOKED || next == CameraEventReservationStatus.EXPIRED
            CameraEventReservationStatus.REVOKED -> return next == CameraEventReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CameraEventReservationStatus) {
        if (!canMoveTo(next)) {
            error("camera event reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round(endsAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): CameraEventReservation {
        return CameraEventReservation(
            code = code,
            title = title,
            batchSize = batchSize,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            locale = locale,
            cost = cost,
            ratio = ratio,
            endsAt = endsAt,
            pallets = pallets,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a camera event reservation in status requested. */
fun newCameraEventReservation(code: String, title: String): CameraEventReservation {
    return CameraEventReservation(
        code = code,
        title = title,
        batchSize = 0,
        reviewer = "",
        durationMinutes = 0,
        locale = "",
        cost = 0.0,
        ratio = 0.0,
        endsAt = 0,
        pallets = 0,
        notes = "",
        status = CameraEventReservationStatus.REQUESTED,
    )
}

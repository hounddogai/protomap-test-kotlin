package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient cutoff reservation is in its life cycle. */
enum class LenientCutoffReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient cutoff reservation, with what the planners need to act on it. */
data class LenientCutoffReservation(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var sequence: Int,
    var attempts: Int,
    var vehicleId: String,
    var status: LenientCutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCutoffReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCutoffReservationStatus): Boolean {
        when (status) {
            LenientCutoffReservationStatus.PLANNED -> return next == LenientCutoffReservationStatus.COMPLETED
            LenientCutoffReservationStatus.SCHEDULED -> {
                when (next) {
                    LenientCutoffReservationStatus.IN_PROGRESS, LenientCutoffReservationStatus.COMPLETED -> return true
                }
                return false
            }
            LenientCutoffReservationStatus.IN_PROGRESS -> {
                when (next) {
                    LenientCutoffReservationStatus.COMPLETED, LenientCutoffReservationStatus.CANCELLED -> return true
                }
                return false
            }
            LenientCutoffReservationStatus.COMPLETED -> {
                when (next) {
                    LenientCutoffReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("lenient cutoff reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round((sequence.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientCutoffReservation {
        return LenientCutoffReservation(
            code = code,
            title = title,
            updatedAt = updatedAt,
            sequence = sequence,
            attempts = attempts,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient cutoff reservation in status planned. */
fun newLenientCutoffReservation(code: String, title: String): LenientCutoffReservation {
    return LenientCutoffReservation(
        code = code,
        title = title,
        updatedAt = 0,
        sequence = 0,
        attempts = 0,
        vehicleId = "",
        status = LenientCutoffReservationStatus.PLANNED,
    )
}

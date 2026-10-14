package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global tire reservation records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a global tire reservation is in its life cycle. */
enum class GlobalTireReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global tire reservation, with what the planners need to act on it. */
data class GlobalTireReservation(
    var code: String,
    var label: String,
    var carrier: String,
    var source: String,
    var etaMinutes: Int,
    var sequence: Int,
    var score: Double,
    var retries: Int,
    var pallets: Int,
    var archived: Boolean,
    var status: GlobalTireReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTireReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTireReservationStatus): Boolean {
        when (status) {
            GlobalTireReservationStatus.PLANNED -> return next == GlobalTireReservationStatus.COMPLETED || next == GlobalTireReservationStatus.CANCELLED
            GlobalTireReservationStatus.SCHEDULED -> {
                when (next) {
                    GlobalTireReservationStatus.IN_PROGRESS, GlobalTireReservationStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalTireReservationStatus.IN_PROGRESS -> return next == GlobalTireReservationStatus.CANCELLED
            GlobalTireReservationStatus.COMPLETED -> {
                when (next) {
                    GlobalTireReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTireReservationStatus) {
        if (!canMoveTo(next)) {
            error("global tire reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round(pallets.toDouble() * score * 100) / 100

    fun snapshot(): GlobalTireReservation {
        return GlobalTireReservation(
            code = code,
            label = label,
            carrier = carrier,
            source = source,
            etaMinutes = etaMinutes,
            sequence = sequence,
            score = score,
            retries = retries,
            pallets = pallets,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global tire reservation in status planned. */
fun newGlobalTireReservation(code: String, label: String): GlobalTireReservation {
    return GlobalTireReservation(
        code = code,
        label = label,
        carrier = "",
        source = "",
        etaMinutes = 0,
        sequence = 0,
        score = 0.0,
        retries = 0,
        pallets = 0,
        archived = false,
        status = GlobalTireReservationStatus.PLANNED,
    )
}

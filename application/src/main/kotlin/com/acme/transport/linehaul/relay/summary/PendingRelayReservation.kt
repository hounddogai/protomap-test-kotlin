package com.acme.transport.linehaul.relay.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending relay reservation is in its life cycle. */
enum class PendingRelayReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending relay reservation as the relay team keeps it. */
data class PendingRelayReservation(
    var code: String,
    var title: String,
    var version: Int,
    var updatedAt: Int,
    var maximum: Double,
    var etaMinutes: Int,
    var durationMinutes: Int,
    var limit: Int,
    var reviewer: String,
    var ceiling: Double,
    var status: PendingRelayReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelayReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelayReservationStatus): Boolean {
        when (status) {
            PendingRelayReservationStatus.PLANNED -> {
                when (next) {
                    PendingRelayReservationStatus.SCHEDULED, PendingRelayReservationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PendingRelayReservationStatus.SCHEDULED -> {
                when (next) {
                    PendingRelayReservationStatus.IN_PROGRESS, PendingRelayReservationStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRelayReservationStatus.IN_PROGRESS -> return next == PendingRelayReservationStatus.COMPLETED
            PendingRelayReservationStatus.COMPLETED -> {
                when (next) {
                    PendingRelayReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRelayReservationStatus) {
        if (!canMoveTo(next)) {
            error("pending relay reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"pending relay reservation"} $code"

    fun adjustedVolume(): Double = round((updatedAt.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingRelayReservation {
        return PendingRelayReservation(
            code = code,
            title = title,
            version = version,
            updatedAt = updatedAt,
            maximum = maximum,
            etaMinutes = etaMinutes,
            durationMinutes = durationMinutes,
            limit = limit,
            reviewer = reviewer,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending relay reservation in status planned. */
fun newPendingRelayReservation(code: String, title: String): PendingRelayReservation {
    return PendingRelayReservation(
        code = code,
        title = title,
        version = 0,
        updatedAt = 0,
        maximum = 0.0,
        etaMinutes = 0,
        durationMinutes = 0,
        limit = 0,
        reviewer = "",
        ceiling = 0.0,
        status = PendingRelayReservationStatus.PLANNED,
    )
}

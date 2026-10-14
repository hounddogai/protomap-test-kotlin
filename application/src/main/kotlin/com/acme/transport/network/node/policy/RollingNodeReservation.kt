package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling node reservation is in its life cycle. */
enum class RollingNodeReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling node reservation as the node team keeps it. */
data class RollingNodeReservation(
    var code: String,
    var label: String,
    var attempts: Int,
    var notes: String,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var endsAt: Int,
    var status: RollingNodeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNodeReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNodeReservationStatus): Boolean {
        when (status) {
            RollingNodeReservationStatus.OPEN -> return next == RollingNodeReservationStatus.RESOLVED
            RollingNodeReservationStatus.IN_REVIEW -> return next == RollingNodeReservationStatus.RESOLVED
            RollingNodeReservationStatus.RESOLVED -> return next == RollingNodeReservationStatus.REOPENED || next == RollingNodeReservationStatus.ARCHIVED
            RollingNodeReservationStatus.REOPENED -> return next == RollingNodeReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingNodeReservationStatus) {
        if (!canMoveTo(next)) {
            error("rolling node reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((attempts.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingNodeReservation {
        return RollingNodeReservation(
            code = code,
            label = label,
            attempts = attempts,
            notes = notes,
            attributes = attributes,
            active = active,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling node reservation in status open. */
fun newRollingNodeReservation(code: String, label: String): RollingNodeReservation {
    return RollingNodeReservation(
        code = code,
        label = label,
        attempts = 0,
        notes = "",
        attributes = mutableMapOf<String, String>(),
        active = false,
        endsAt = 0,
        status = RollingNodeReservationStatus.OPEN,
    )
}

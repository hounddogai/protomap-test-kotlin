package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global tender reservation records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a global tender reservation is in its life cycle. */
enum class GlobalTenderReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global tender reservation as the tender team keeps it. */
data class GlobalTenderReservation(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var odometer: Int,
    var shiftCode: String,
    var score: Double,
    var version: Int,
    var pinned: Boolean,
    var minimum: Double,
    var status: GlobalTenderReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderReservationStatus): Boolean {
        when (status) {
            GlobalTenderReservationStatus.IDLE -> return next == GlobalTenderReservationStatus.DRAINING
            GlobalTenderReservationStatus.WARMING -> return next == GlobalTenderReservationStatus.READY || next == GlobalTenderReservationStatus.STOPPED
            GlobalTenderReservationStatus.READY -> return next == GlobalTenderReservationStatus.DRAINING
            GlobalTenderReservationStatus.DRAINING -> {
                when (next) {
                    GlobalTenderReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTenderReservationStatus) {
        if (!canMoveTo(next)) {
            error("global tender reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((odometer.toDouble() + minimum) * 100) / 100

    fun snapshot(): GlobalTenderReservation {
        return GlobalTenderReservation(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            odometer = odometer,
            shiftCode = shiftCode,
            score = score,
            version = version,
            pinned = pinned,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global tender reservation in status idle. */
fun newGlobalTenderReservation(code: String, name: String): GlobalTenderReservation {
    return GlobalTenderReservation(
        code = code,
        name = name,
        durationMinutes = 0,
        odometer = 0,
        shiftCode = "",
        score = 0.0,
        version = 0,
        pinned = false,
        minimum = 0.0,
        status = GlobalTenderReservationStatus.IDLE,
    )
}

package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tire reservation is in its life cycle. */
enum class RollingTireReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling tire reservation, with what the planners need to act on it. */
data class RollingTireReservation(
    var code: String,
    var title: String,
    var attempts: Int,
    var unit: String,
    var pallets: Int,
    var fuelLiters: Double,
    var vehicleId: String,
    var summary: String,
    var status: RollingTireReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTireReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTireReservationStatus): Boolean {
        when (status) {
            RollingTireReservationStatus.PROPOSED -> return next == RollingTireReservationStatus.APPROVED
            RollingTireReservationStatus.APPROVED -> return next == RollingTireReservationStatus.WITHDRAWN
            RollingTireReservationStatus.REJECTED -> {
                when (next) {
                    RollingTireReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTireReservationStatus) {
        if (!canMoveTo(next)) {
            error("rolling tire reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((fuelLiters - attempts.toDouble()) * 100) / 100

    fun snapshot(): RollingTireReservation {
        return RollingTireReservation(
            code = code,
            title = title,
            attempts = attempts,
            unit = unit,
            pallets = pallets,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling tire reservation in status proposed. */
fun newRollingTireReservation(code: String, title: String): RollingTireReservation {
    return RollingTireReservation(
        code = code,
        title = title,
        attempts = 0,
        unit = "",
        pallets = 0,
        fuelLiters = 0.0,
        vehicleId = "",
        summary = "",
        status = RollingTireReservationStatus.PROPOSED,
    )
}

package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy backhaul reservation records of linehaul's backhaul work, and the rules, stores, and
// calculations around them.

/** Where a legacy backhaul reservation is in its life cycle. */
enum class LegacyBackhaulReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy backhaul reservation as the backhaul team keeps it. */
data class LegacyBackhaulReservation(
    var code: String,
    var name: String,
    var routeCode: String,
    var vehicleId: String,
    var description: String,
    var createdAt: Int,
    var attempts: Int,
    var etaMinutes: Int,
    var status: LegacyBackhaulReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulReservationStatus): Boolean {
        when (status) {
            LegacyBackhaulReservationStatus.IDLE -> {
                when (next) {
                    LegacyBackhaulReservationStatus.WARMING -> return true
                }
                return false
            }
            LegacyBackhaulReservationStatus.WARMING -> {
                when (next) {
                    LegacyBackhaulReservationStatus.READY, LegacyBackhaulReservationStatus.STOPPED -> return true
                }
                return false
            }
            LegacyBackhaulReservationStatus.READY -> {
                when (next) {
                    LegacyBackhaulReservationStatus.DRAINING, LegacyBackhaulReservationStatus.STOPPED -> return true
                }
                return false
            }
            LegacyBackhaulReservationStatus.DRAINING -> return next == LegacyBackhaulReservationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((createdAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): LegacyBackhaulReservation {
        return LegacyBackhaulReservation(
            code = code,
            name = name,
            routeCode = routeCode,
            vehicleId = vehicleId,
            description = description,
            createdAt = createdAt,
            attempts = attempts,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy backhaul reservation in status idle. */
fun newLegacyBackhaulReservation(code: String, name: String): LegacyBackhaulReservation {
    return LegacyBackhaulReservation(
        code = code,
        name = name,
        routeCode = "",
        vehicleId = "",
        description = "",
        createdAt = 0,
        attempts = 0,
        etaMinutes = 0,
        status = LegacyBackhaulReservationStatus.IDLE,
    )
}

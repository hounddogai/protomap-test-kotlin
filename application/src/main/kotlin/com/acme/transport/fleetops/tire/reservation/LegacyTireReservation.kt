package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy tire reservation records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a legacy tire reservation is in its life cycle. */
enum class LegacyTireReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy tire reservation. Its code is unique within a tenant. */
data class LegacyTireReservation(
    var code: String,
    var name: String,
    var trailerId: String,
    var weight: Double,
    var routeCode: String,
    var batchSize: Int,
    var etaMinutes: Int,
    var active: Boolean,
    var visible: Boolean,
    var retries: Int,
    var status: LegacyTireReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTireReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTireReservationStatus): Boolean {
        when (status) {
            LegacyTireReservationStatus.IDLE -> {
                when (next) {
                    LegacyTireReservationStatus.READY, LegacyTireReservationStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTireReservationStatus.WARMING -> {
                when (next) {
                    LegacyTireReservationStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTireReservationStatus.READY -> {
                when (next) {
                    LegacyTireReservationStatus.DRAINING, LegacyTireReservationStatus.STOPPED -> return true
                }
                return false
            }
            LegacyTireReservationStatus.DRAINING -> {
                when (next) {
                    LegacyTireReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTireReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy tire reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy tire reservation"} $code"

    fun effectiveLoad(): Double = round(batchSize.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): LegacyTireReservation {
        return LegacyTireReservation(
            code = code,
            name = name,
            trailerId = trailerId,
            weight = weight,
            routeCode = routeCode,
            batchSize = batchSize,
            etaMinutes = etaMinutes,
            active = active,
            visible = visible,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy tire reservation in status idle. */
fun newLegacyTireReservation(code: String, name: String): LegacyTireReservation {
    return LegacyTireReservation(
        code = code,
        name = name,
        trailerId = "",
        weight = 0.0,
        routeCode = "",
        batchSize = 0,
        etaMinutes = 0,
        active = false,
        visible = false,
        retries = 0,
        status = LegacyTireReservationStatus.IDLE,
    )
}

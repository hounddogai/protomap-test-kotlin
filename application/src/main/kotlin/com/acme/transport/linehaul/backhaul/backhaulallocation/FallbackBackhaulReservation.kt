package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback backhaul reservation is in its life cycle. */
enum class FallbackBackhaulReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback backhaul reservation and its current status. */
data class FallbackBackhaulReservation(
    var code: String,
    var title: String,
    var notes: String,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var odometer: Int,
    var region: String,
    var graceDays: Int,
    var driverId: String,
    var locale: String,
    var status: FallbackBackhaulReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulReservationStatus): Boolean {
        when (status) {
            FallbackBackhaulReservationStatus.IDLE -> {
                when (next) {
                    FallbackBackhaulReservationStatus.WARMING, FallbackBackhaulReservationStatus.STOPPED -> return true
                }
                return false
            }
            FallbackBackhaulReservationStatus.WARMING -> {
                when (next) {
                    FallbackBackhaulReservationStatus.READY, FallbackBackhaulReservationStatus.DRAINING -> return true
                }
                return false
            }
            FallbackBackhaulReservationStatus.READY -> {
                when (next) {
                    FallbackBackhaulReservationStatus.DRAINING -> return true
                }
                return false
            }
            FallbackBackhaulReservationStatus.DRAINING -> {
                when (next) {
                    FallbackBackhaulReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulReservationStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((graceDays.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): FallbackBackhaulReservation {
        return FallbackBackhaulReservation(
            code = code,
            title = title,
            notes = notes,
            attributes = attributes,
            pinned = pinned,
            odometer = odometer,
            region = region,
            graceDays = graceDays,
            driverId = driverId,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback backhaul reservation in status idle. */
fun newFallbackBackhaulReservation(code: String, title: String): FallbackBackhaulReservation {
    return FallbackBackhaulReservation(
        code = code,
        title = title,
        notes = "",
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        odometer = 0,
        region = "",
        graceDays = 0,
        driverId = "",
        locale = "",
        status = FallbackBackhaulReservationStatus.IDLE,
    )
}

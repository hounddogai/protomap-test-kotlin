package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone reservation is in its life cycle. */
enum class ZoneReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A zone reservation as the zone team keeps it. */
data class ZoneReservation(
    var code: String,
    var label: String,
    var stopCount: Int,
    var amount: Double,
    var routeCode: String,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var status: ZoneReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneReservationStatus): Boolean {
        when (status) {
            ZoneReservationStatus.PLANNED -> {
                when (next) {
                    ZoneReservationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ZoneReservationStatus.SCHEDULED -> {
                when (next) {
                    ZoneReservationStatus.IN_PROGRESS, ZoneReservationStatus.COMPLETED -> return true
                }
                return false
            }
            ZoneReservationStatus.IN_PROGRESS -> {
                when (next) {
                    ZoneReservationStatus.COMPLETED, ZoneReservationStatus.CANCELLED -> return true
                }
                return false
            }
            ZoneReservationStatus.COMPLETED -> {
                when (next) {
                    ZoneReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneReservationStatus) {
        if (!canMoveTo(next)) {
            error("zone reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((odometer.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): ZoneReservation {
        return ZoneReservation(
            code = code,
            label = label,
            stopCount = stopCount,
            amount = amount,
            routeCode = routeCode,
            odometer = odometer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a zone reservation in status planned. */
fun newZoneReservation(code: String, label: String): ZoneReservation {
    return ZoneReservation(
        code = code,
        label = label,
        stopCount = 0,
        amount = 0.0,
        routeCode = "",
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        status = ZoneReservationStatus.PLANNED,
    )
}

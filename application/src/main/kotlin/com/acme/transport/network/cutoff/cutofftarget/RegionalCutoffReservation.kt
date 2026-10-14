package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cutoff reservation is in its life cycle. */
enum class RegionalCutoffReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional cutoff reservation, with what the planners need to act on it. */
data class RegionalCutoffReservation(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var odometer: Int,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var status: RegionalCutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCutoffReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCutoffReservationStatus): Boolean {
        when (status) {
            RegionalCutoffReservationStatus.DRAFT -> {
                when (next) {
                    RegionalCutoffReservationStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCutoffReservationStatus.PENDING -> return next == RegionalCutoffReservationStatus.CLOSED
            RegionalCutoffReservationStatus.ACTIVE -> return next == RegionalCutoffReservationStatus.SUSPENDED || next == RegionalCutoffReservationStatus.CLOSED
            RegionalCutoffReservationStatus.SUSPENDED -> {
                when (next) {
                    RegionalCutoffReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("regional cutoff reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((odometer.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalCutoffReservation {
        return RegionalCutoffReservation(
            code = code,
            name = name,
            expiresAt = expiresAt,
            odometer = odometer,
            timezone = timezone,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional cutoff reservation in status draft. */
fun newRegionalCutoffReservation(code: String, name: String): RegionalCutoffReservation {
    return RegionalCutoffReservation(
        code = code,
        name = name,
        expiresAt = 0,
        odometer = 0,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        status = RegionalCutoffReservationStatus.DRAFT,
    )
}

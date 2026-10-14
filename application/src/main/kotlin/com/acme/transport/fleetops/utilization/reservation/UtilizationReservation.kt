package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization reservation records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization reservation is in its life cycle. */
enum class UtilizationReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A utilization reservation. Its code is unique within a tenant. */
data class UtilizationReservation(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var sequence: Int,
    var ownerId: String,
    var status: UtilizationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationReservationStatus): Boolean {
        when (status) {
            UtilizationReservationStatus.PLANNED -> {
                when (next) {
                    UtilizationReservationStatus.SCHEDULED, UtilizationReservationStatus.IN_PROGRESS -> return true
                }
                return false
            }
            UtilizationReservationStatus.SCHEDULED -> {
                when (next) {
                    UtilizationReservationStatus.IN_PROGRESS, UtilizationReservationStatus.CANCELLED -> return true
                }
                return false
            }
            UtilizationReservationStatus.IN_PROGRESS -> {
                when (next) {
                    UtilizationReservationStatus.COMPLETED -> return true
                }
                return false
            }
            UtilizationReservationStatus.COMPLETED -> {
                when (next) {
                    UtilizationReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationReservationStatus) {
        if (!canMoveTo(next)) {
            error("utilization reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((expiresAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): UtilizationReservation {
        return UtilizationReservation(
            code = code,
            title = title,
            expiresAt = expiresAt,
            attributes = attributes,
            depot = depot,
            sequence = sequence,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a utilization reservation in status planned. */
fun newUtilizationReservation(code: String, title: String): UtilizationReservation {
    return UtilizationReservation(
        code = code,
        title = title,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        sequence = 0,
        ownerId = "",
        status = UtilizationReservationStatus.PLANNED,
    )
}

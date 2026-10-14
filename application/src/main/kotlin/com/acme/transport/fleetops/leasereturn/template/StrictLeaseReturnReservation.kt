package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict lease return reservation is in its life cycle. */
enum class StrictLeaseReturnReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One strict lease return reservation, with what the planners need to act on it. */
data class StrictLeaseReturnReservation(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var stopCount: Int,
    var margin: Double,
    var approved: Boolean,
    var loadKg: Double,
    var version: Int,
    var category: String,
    var status: StrictLeaseReturnReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLeaseReturnReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLeaseReturnReservationStatus): Boolean {
        when (status) {
            StrictLeaseReturnReservationStatus.PLANNED -> return next == StrictLeaseReturnReservationStatus.IN_PROGRESS || next == StrictLeaseReturnReservationStatus.COMPLETED
            StrictLeaseReturnReservationStatus.SCHEDULED -> {
                when (next) {
                    StrictLeaseReturnReservationStatus.COMPLETED, StrictLeaseReturnReservationStatus.CANCELLED -> return true
                }
                return false
            }
            StrictLeaseReturnReservationStatus.IN_PROGRESS -> return next == StrictLeaseReturnReservationStatus.COMPLETED || next == StrictLeaseReturnReservationStatus.CANCELLED
            StrictLeaseReturnReservationStatus.COMPLETED -> return next == StrictLeaseReturnReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: StrictLeaseReturnReservationStatus) {
        if (!canMoveTo(next)) {
            error("strict lease return reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"strict lease return reservation"} $code"

    fun adjustedTotal(): Double = round(version.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): StrictLeaseReturnReservation {
        return StrictLeaseReturnReservation(
            code = code,
            name = name,
            updatedAt = updatedAt,
            stopCount = stopCount,
            margin = margin,
            approved = approved,
            loadKg = loadKg,
            version = version,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict lease return reservation in status planned. */
fun newStrictLeaseReturnReservation(code: String, name: String): StrictLeaseReturnReservation {
    return StrictLeaseReturnReservation(
        code = code,
        name = name,
        updatedAt = 0,
        stopCount = 0,
        margin = 0.0,
        approved = false,
        loadKg = 0.0,
        version = 0,
        category = "",
        status = StrictLeaseReturnReservationStatus.PLANNED,
    )
}

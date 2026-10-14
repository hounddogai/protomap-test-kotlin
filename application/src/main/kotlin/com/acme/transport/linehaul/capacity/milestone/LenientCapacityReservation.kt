package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient capacity reservation is in its life cycle. */
enum class LenientCapacityReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient capacity reservation. Its code is unique within a tenant. */
data class LenientCapacityReservation(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var vehicleId: String,
    var depot: String,
    var batchSize: Int,
    var weight: Double,
    var updatedAt: Int,
    var retries: Int,
    var country: String,
    var status: LenientCapacityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCapacityReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCapacityReservationStatus): Boolean {
        when (status) {
            LenientCapacityReservationStatus.REQUESTED -> return next == LenientCapacityReservationStatus.GRANTED || next == LenientCapacityReservationStatus.EXPIRED
            LenientCapacityReservationStatus.GRANTED -> {
                when (next) {
                    LenientCapacityReservationStatus.REVOKED -> return true
                }
                return false
            }
            LenientCapacityReservationStatus.REVOKED -> {
                when (next) {
                    LenientCapacityReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCapacityReservationStatus) {
        if (!canMoveTo(next)) {
            error("lenient capacity reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient capacity reservation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((retries.toDouble() - weight) * 100) / 100

    fun snapshot(): LenientCapacityReservation {
        return LenientCapacityReservation(
            code = code,
            title = title,
            tags = tags,
            vehicleId = vehicleId,
            depot = depot,
            batchSize = batchSize,
            weight = weight,
            updatedAt = updatedAt,
            retries = retries,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient capacity reservation in status requested. */
fun newLenientCapacityReservation(code: String, title: String): LenientCapacityReservation {
    return LenientCapacityReservation(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        vehicleId = "",
        depot = "",
        batchSize = 0,
        weight = 0.0,
        updatedAt = 0,
        retries = 0,
        country = "",
        status = LenientCapacityReservationStatus.REQUESTED,
    )
}

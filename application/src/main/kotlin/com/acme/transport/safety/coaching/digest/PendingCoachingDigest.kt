package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending coaching digest records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a pending coaching digest is in its life cycle. */
enum class PendingCoachingDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending coaching digest and its current status. */
data class PendingCoachingDigest(
    var code: String,
    var name: String,
    var odometer: Int,
    var vehicleId: String,
    var capacity: Int,
    var minimum: Double,
    var region: String,
    var maximum: Double,
    var trailerId: String,
    var archived: Boolean,
    var status: PendingCoachingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCoachingDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCoachingDigestStatus): Boolean {
        when (status) {
            PendingCoachingDigestStatus.REQUESTED -> {
                when (next) {
                    PendingCoachingDigestStatus.REVOKED, PendingCoachingDigestStatus.EXPIRED -> return true
                }
                return false
            }
            PendingCoachingDigestStatus.GRANTED -> {
                when (next) {
                    PendingCoachingDigestStatus.EXPIRED -> return true
                }
                return false
            }
            PendingCoachingDigestStatus.REVOKED -> {
                when (next) {
                    PendingCoachingDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCoachingDigestStatus) {
        if (!canMoveTo(next)) {
            error("pending coaching digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round(odometer.toDouble() * minimum * 100) / 100

    fun snapshot(): PendingCoachingDigest {
        return PendingCoachingDigest(
            code = code,
            name = name,
            odometer = odometer,
            vehicleId = vehicleId,
            capacity = capacity,
            minimum = minimum,
            region = region,
            maximum = maximum,
            trailerId = trailerId,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending coaching digest in status requested. */
fun newPendingCoachingDigest(code: String, name: String): PendingCoachingDigest {
    return PendingCoachingDigest(
        code = code,
        name = name,
        odometer = 0,
        vehicleId = "",
        capacity = 0,
        minimum = 0.0,
        region = "",
        maximum = 0.0,
        trailerId = "",
        archived = false,
        status = PendingCoachingDigestStatus.REQUESTED,
    )
}

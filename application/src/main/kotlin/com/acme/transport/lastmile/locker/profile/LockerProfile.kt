package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker profile records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker profile is in its life cycle. */
enum class LockerProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A locker profile. Its code is unique within a tenant. */
data class LockerProfile(
    var code: String,
    var name: String,
    var driverId: String,
    var vehicleId: String,
    var margin: Double,
    var version: Int,
    var lane: String,
    var externalRef: String,
    var minimum: Double,
    var fuelLiters: Double,
    var score: Double,
    var status: LockerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerProfileStatus): Boolean {
        when (status) {
            LockerProfileStatus.PLANNED -> {
                when (next) {
                    LockerProfileStatus.SCHEDULED, LockerProfileStatus.COMPLETED -> return true
                }
                return false
            }
            LockerProfileStatus.SCHEDULED -> {
                when (next) {
                    LockerProfileStatus.IN_PROGRESS, LockerProfileStatus.CANCELLED -> return true
                }
                return false
            }
            LockerProfileStatus.IN_PROGRESS -> return next == LockerProfileStatus.COMPLETED
            LockerProfileStatus.COMPLETED -> return next == LockerProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LockerProfileStatus) {
        if (!canMoveTo(next)) {
            error("locker profile $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"locker profile"} $code"

    fun projectedExposure(): Double = round((score - margin) * 100) / 100

    fun snapshot(): LockerProfile {
        return LockerProfile(
            code = code,
            name = name,
            driverId = driverId,
            vehicleId = vehicleId,
            margin = margin,
            version = version,
            lane = lane,
            externalRef = externalRef,
            minimum = minimum,
            fuelLiters = fuelLiters,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker profile in status planned. */
fun newLockerProfile(code: String, name: String): LockerProfile {
    return LockerProfile(
        code = code,
        name = name,
        driverId = "",
        vehicleId = "",
        margin = 0.0,
        version = 0,
        lane = "",
        externalRef = "",
        minimum = 0.0,
        fuelLiters = 0.0,
        score = 0.0,
        status = LockerProfileStatus.PLANNED,
    )
}

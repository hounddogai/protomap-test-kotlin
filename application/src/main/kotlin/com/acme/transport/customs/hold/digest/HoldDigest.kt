package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold digest records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold digest is in its life cycle. */
enum class HoldDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One hold digest, with what the planners need to act on it. */
data class HoldDigest(
    var code: String,
    var label: String,
    var vehicleId: String,
    var lane: String,
    var durationMinutes: Int,
    var driverId: String,
    var dock: String,
    var country: String,
    var distanceKm: Double,
    var windowDays: Int,
    var status: HoldDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldDigestStatus): Boolean {
        when (status) {
            HoldDigestStatus.CREATED -> {
                when (next) {
                    HoldDigestStatus.PUBLISHED -> return true
                }
                return false
            }
            HoldDigestStatus.VALIDATED -> {
                when (next) {
                    HoldDigestStatus.PUBLISHED, HoldDigestStatus.RETIRED -> return true
                }
                return false
            }
            HoldDigestStatus.PUBLISHED -> {
                when (next) {
                    HoldDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldDigestStatus) {
        if (!canMoveTo(next)) {
            error("hold digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round(distanceKm * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): HoldDigest {
        return HoldDigest(
            code = code,
            label = label,
            vehicleId = vehicleId,
            lane = lane,
            durationMinutes = durationMinutes,
            driverId = driverId,
            dock = dock,
            country = country,
            distanceKm = distanceKm,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hold digest in status created. */
fun newHoldDigest(code: String, label: String): HoldDigest {
    return HoldDigest(
        code = code,
        label = label,
        vehicleId = "",
        lane = "",
        durationMinutes = 0,
        driverId = "",
        dock = "",
        country = "",
        distanceKm = 0.0,
        windowDays = 0,
        status = HoldDigestStatus.CREATED,
    )
}

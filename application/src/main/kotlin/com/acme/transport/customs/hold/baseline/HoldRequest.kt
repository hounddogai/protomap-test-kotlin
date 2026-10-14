package com.acme.transport.customs.hold.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold request is in its life cycle. */
enum class HoldRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One hold request, with what the planners need to act on it. */
data class HoldRequest(
    var code: String,
    var name: String,
    var version: Int,
    var durationMinutes: Int,
    var region: String,
    var createdAt: Int,
    var odometer: Int,
    var startsAt: Int,
    var fuelLiters: Double,
    var locale: String,
    var status: HoldRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldRequestStatus): Boolean {
        when (status) {
            HoldRequestStatus.IDLE -> return next == HoldRequestStatus.READY
            HoldRequestStatus.WARMING -> return next == HoldRequestStatus.DRAINING
            HoldRequestStatus.READY -> {
                when (next) {
                    HoldRequestStatus.DRAINING, HoldRequestStatus.STOPPED -> return true
                }
                return false
            }
            HoldRequestStatus.DRAINING -> {
                when (next) {
                    HoldRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldRequestStatus) {
        if (!canMoveTo(next)) {
            error("hold request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun projectedLoad(): Double = round(startsAt.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): HoldRequest {
        return HoldRequest(
            code = code,
            name = name,
            version = version,
            durationMinutes = durationMinutes,
            region = region,
            createdAt = createdAt,
            odometer = odometer,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a hold request in status idle. */
fun newHoldRequest(code: String, name: String): HoldRequest {
    return HoldRequest(
        code = code,
        name = name,
        version = 0,
        durationMinutes = 0,
        region = "",
        createdAt = 0,
        odometer = 0,
        startsAt = 0,
        fuelLiters = 0.0,
        locale = "",
        status = HoldRequestStatus.IDLE,
    )
}

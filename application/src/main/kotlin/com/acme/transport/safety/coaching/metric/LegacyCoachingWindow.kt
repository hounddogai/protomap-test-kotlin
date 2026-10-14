package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy coaching window is in its life cycle. */
enum class LegacyCoachingWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy coaching window, with what the planners need to act on it. */
data class LegacyCoachingWindow(
    var code: String,
    var label: String,
    var limit: Int,
    var batchSize: Int,
    var phone: String,
    var margin: Double,
    var vehicleId: String,
    var fuelLiters: Double,
    var source: String,
    var endsAt: Int,
    var trailerId: String,
    var status: LegacyCoachingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCoachingWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCoachingWindowStatus): Boolean {
        when (status) {
            LegacyCoachingWindowStatus.REQUESTED -> return next == LegacyCoachingWindowStatus.GRANTED
            LegacyCoachingWindowStatus.GRANTED -> {
                when (next) {
                    LegacyCoachingWindowStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyCoachingWindowStatus.REVOKED -> {
                when (next) {
                    LegacyCoachingWindowStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyCoachingWindowStatus) {
        if (!canMoveTo(next)) {
            error("legacy coaching window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy coaching window"} $code"

    fun weightedVolume(): Double = round(limit.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): LegacyCoachingWindow {
        return LegacyCoachingWindow(
            code = code,
            label = label,
            limit = limit,
            batchSize = batchSize,
            phone = phone,
            margin = margin,
            vehicleId = vehicleId,
            fuelLiters = fuelLiters,
            source = source,
            endsAt = endsAt,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a legacy coaching window in status requested. */
fun newLegacyCoachingWindow(code: String, label: String): LegacyCoachingWindow {
    return LegacyCoachingWindow(
        code = code,
        label = label,
        limit = 0,
        batchSize = 0,
        phone = "",
        margin = 0.0,
        vehicleId = "",
        fuelLiters = 0.0,
        source = "",
        endsAt = 0,
        trailerId = "",
        status = LegacyCoachingWindowStatus.REQUESTED,
    )
}

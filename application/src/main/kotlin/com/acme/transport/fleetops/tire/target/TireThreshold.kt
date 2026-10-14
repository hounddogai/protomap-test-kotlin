package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire threshold is in its life cycle. */
enum class TireThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tire threshold, with what the planners need to act on it. */
data class TireThreshold(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var vehicleId: String,
    var score: Double,
    var maximum: Double,
    var carrier: String,
    var checksum: String,
    var capacity: Int,
    var etaMinutes: Int,
    var reviewer: String,
    var status: TireThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireThresholdStatus): Boolean {
        when (status) {
            TireThresholdStatus.PLANNED -> {
                when (next) {
                    TireThresholdStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TireThresholdStatus.SCHEDULED -> return next == TireThresholdStatus.COMPLETED || next == TireThresholdStatus.CANCELLED
            TireThresholdStatus.IN_PROGRESS -> return next == TireThresholdStatus.COMPLETED || next == TireThresholdStatus.CANCELLED
            TireThresholdStatus.COMPLETED -> return next == TireThresholdStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TireThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tire threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tire threshold"} $code"

    fun effectiveExposure(): Double = round(etaMinutes.toDouble() * score * 100) / 100

    fun snapshot(): TireThreshold {
        return TireThreshold(
            code = code,
            title = title,
            expiresAt = expiresAt,
            vehicleId = vehicleId,
            score = score,
            maximum = maximum,
            carrier = carrier,
            checksum = checksum,
            capacity = capacity,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tire threshold in status planned. */
fun newTireThreshold(code: String, title: String): TireThreshold {
    return TireThreshold(
        code = code,
        title = title,
        expiresAt = 0,
        vehicleId = "",
        score = 0.0,
        maximum = 0.0,
        carrier = "",
        checksum = "",
        capacity = 0,
        etaMinutes = 0,
        reviewer = "",
        status = TireThresholdStatus.PLANNED,
    )
}

package com.acme.transport.safety.coaching.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching threshold records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching threshold is in its life cycle. */
enum class CoachingThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a coaching threshold and its current status. */
data class CoachingThreshold(
    var code: String,
    var name: String,
    var trailerId: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var floorValue: Double,
    var status: CoachingThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingThresholdStatus): Boolean {
        when (status) {
            CoachingThresholdStatus.PLANNED -> {
                when (next) {
                    CoachingThresholdStatus.SCHEDULED, CoachingThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingThresholdStatus.SCHEDULED -> {
                when (next) {
                    CoachingThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingThresholdStatus.IN_PROGRESS -> return next == CoachingThresholdStatus.CANCELLED
            CoachingThresholdStatus.COMPLETED -> return next == CoachingThresholdStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CoachingThresholdStatus) {
        if (!canMoveTo(next)) {
            error("coaching threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round(createdAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): CoachingThreshold {
        return CoachingThreshold(
            code = code,
            name = name,
            trailerId = trailerId,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching threshold in status planned. */
fun newCoachingThreshold(code: String, name: String): CoachingThreshold {
    return CoachingThreshold(
        code = code,
        name = name,
        trailerId = "",
        durationMinutes = 0,
        createdAt = 0,
        floorValue = 0.0,
        status = CoachingThresholdStatus.PLANNED,
    )
}

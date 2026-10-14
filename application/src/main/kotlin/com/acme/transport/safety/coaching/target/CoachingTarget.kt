package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching target records of safety's coaching work, and the rules, stores, and calculations around them.

/** Where a coaching target is in its life cycle. */
enum class CoachingTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A coaching target as the coaching team keeps it. */
data class CoachingTarget(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var floorValue: Double,
    var weight: Double,
    var ratio: Double,
    var limit: Int,
    var status: CoachingTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingTargetStatus): Boolean {
        when (status) {
            CoachingTargetStatus.PLANNED -> {
                when (next) {
                    CoachingTargetStatus.COMPLETED -> return true
                }
                return false
            }
            CoachingTargetStatus.SCHEDULED -> {
                when (next) {
                    CoachingTargetStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingTargetStatus.IN_PROGRESS -> return next == CoachingTargetStatus.COMPLETED
            CoachingTargetStatus.COMPLETED -> return next == CoachingTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CoachingTargetStatus) {
        if (!canMoveTo(next)) {
            error("coaching target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching target"} $code"

    fun projectedVolume(): Double = round((distanceKm - ratio) * 100) / 100

    fun snapshot(): CoachingTarget {
        return CoachingTarget(
            code = code,
            title = title,
            distanceKm = distanceKm,
            floorValue = floorValue,
            weight = weight,
            ratio = ratio,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching target in status planned. */
fun newCoachingTarget(code: String, title: String): CoachingTarget {
    return CoachingTarget(
        code = code,
        title = title,
        distanceKm = 0.0,
        floorValue = 0.0,
        weight = 0.0,
        ratio = 0.0,
        limit = 0,
        status = CoachingTargetStatus.PLANNED,
    )
}

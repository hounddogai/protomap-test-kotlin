package com.acme.transport.safety.coaching.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching milestone is in its life cycle. */
enum class CoachingMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One coaching milestone, with what the planners need to act on it. */
data class CoachingMilestone(
    var code: String,
    var name: String,
    var active: Boolean,
    var carrier: String,
    var fuelLiters: Double,
    var priority: Int,
    var archived: Boolean,
    var stopCount: Int,
    var status: CoachingMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingMilestoneStatus): Boolean {
        when (status) {
            CoachingMilestoneStatus.PLANNED -> return next == CoachingMilestoneStatus.COMPLETED
            CoachingMilestoneStatus.SCHEDULED -> return next == CoachingMilestoneStatus.COMPLETED || next == CoachingMilestoneStatus.CANCELLED
            CoachingMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    CoachingMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            CoachingMilestoneStatus.COMPLETED -> {
                when (next) {
                    CoachingMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("coaching milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching milestone"} $code"

    fun adjustedLoad(): Double = round((stopCount.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): CoachingMilestone {
        return CoachingMilestone(
            code = code,
            name = name,
            active = active,
            carrier = carrier,
            fuelLiters = fuelLiters,
            priority = priority,
            archived = archived,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a coaching milestone in status planned. */
fun newCoachingMilestone(code: String, name: String): CoachingMilestone {
    return CoachingMilestone(
        code = code,
        name = name,
        active = false,
        carrier = "",
        fuelLiters = 0.0,
        priority = 0,
        archived = false,
        stopCount = 0,
        status = CoachingMilestoneStatus.PLANNED,
    )
}

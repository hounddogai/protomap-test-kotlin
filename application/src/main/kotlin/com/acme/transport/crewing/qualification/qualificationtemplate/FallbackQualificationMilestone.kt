package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback qualification milestone is in its life cycle. */
enum class FallbackQualificationMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback qualification milestone, with what the planners need to act on it. */
data class FallbackQualificationMilestone(
    var code: String,
    var title: String,
    var maximum: Double,
    var threshold: Double,
    var updatedAt: Int,
    var limit: Int,
    var minimum: Double,
    var channel: String,
    var quantity: Int,
    var status: FallbackQualificationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackQualificationMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackQualificationMilestoneStatus): Boolean {
        when (status) {
            FallbackQualificationMilestoneStatus.IDLE -> return next == FallbackQualificationMilestoneStatus.STOPPED
            FallbackQualificationMilestoneStatus.WARMING -> {
                when (next) {
                    FallbackQualificationMilestoneStatus.DRAINING -> return true
                }
                return false
            }
            FallbackQualificationMilestoneStatus.READY -> return next == FallbackQualificationMilestoneStatus.DRAINING
            FallbackQualificationMilestoneStatus.DRAINING -> return next == FallbackQualificationMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackQualificationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback qualification milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback qualification milestone"} $code"

    fun projectedValue(): Double = round((updatedAt.toDouble() + threshold) * 100) / 100

    fun snapshot(): FallbackQualificationMilestone {
        return FallbackQualificationMilestone(
            code = code,
            title = title,
            maximum = maximum,
            threshold = threshold,
            updatedAt = updatedAt,
            limit = limit,
            minimum = minimum,
            channel = channel,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback qualification milestone in status idle. */
fun newFallbackQualificationMilestone(code: String, title: String): FallbackQualificationMilestone {
    return FallbackQualificationMilestone(
        code = code,
        title = title,
        maximum = 0.0,
        threshold = 0.0,
        updatedAt = 0,
        limit = 0,
        minimum = 0.0,
        channel = "",
        quantity = 0,
        status = FallbackQualificationMilestoneStatus.IDLE,
    )
}

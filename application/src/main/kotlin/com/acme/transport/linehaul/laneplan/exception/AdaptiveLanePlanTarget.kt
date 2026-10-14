package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive lane plan target is in its life cycle. */
enum class AdaptiveLanePlanTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive lane plan target as the lane plan team keeps it. */
data class AdaptiveLanePlanTarget(
    var code: String,
    var name: String,
    var archived: Boolean,
    var reviewer: String,
    var category: String,
    var attempts: Int,
    var windowDays: Int,
    var threshold: Double,
    var trailerId: String,
    var status: AdaptiveLanePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveLanePlanTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveLanePlanTargetStatus): Boolean {
        when (status) {
            AdaptiveLanePlanTargetStatus.PLANNED -> {
                when (next) {
                    AdaptiveLanePlanTargetStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveLanePlanTargetStatus.SCHEDULED -> return next == AdaptiveLanePlanTargetStatus.IN_PROGRESS || next == AdaptiveLanePlanTargetStatus.COMPLETED
            AdaptiveLanePlanTargetStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveLanePlanTargetStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveLanePlanTargetStatus.COMPLETED -> return next == AdaptiveLanePlanTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveLanePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive lane plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive lane plan target"} $code"

    fun projectedTotal(): Double = round(threshold * attempts.toDouble() * 100) / 100

    fun snapshot(): AdaptiveLanePlanTarget {
        return AdaptiveLanePlanTarget(
            code = code,
            name = name,
            archived = archived,
            reviewer = reviewer,
            category = category,
            attempts = attempts,
            windowDays = windowDays,
            threshold = threshold,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive lane plan target in status planned. */
fun newAdaptiveLanePlanTarget(code: String, name: String): AdaptiveLanePlanTarget {
    return AdaptiveLanePlanTarget(
        code = code,
        name = name,
        archived = false,
        reviewer = "",
        category = "",
        attempts = 0,
        windowDays = 0,
        threshold = 0.0,
        trailerId = "",
        status = AdaptiveLanePlanTargetStatus.PLANNED,
    )
}

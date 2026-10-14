package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.text.normalize

/** Where a trailer plan is in its life cycle. */
enum class TrailerPlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer plan as the trailer team keeps it. */
data class TrailerPlan(
    var code: String,
    var title: String,
    var trailerId: String,
    var externalRef: String,
    var loadKg: Double,
    var reviewer: String,
    var status: TrailerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerPlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerPlanStatus): Boolean {
        when (status) {
            TrailerPlanStatus.OPEN -> return next == TrailerPlanStatus.REOPENED
            TrailerPlanStatus.IN_REVIEW -> {
                when (next) {
                    TrailerPlanStatus.RESOLVED, TrailerPlanStatus.REOPENED -> return true
                }
                return false
            }
            TrailerPlanStatus.RESOLVED -> return next == TrailerPlanStatus.ARCHIVED
            TrailerPlanStatus.REOPENED -> {
                when (next) {
                    TrailerPlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerPlanStatus) {
        if (!canMoveTo(next)) {
            error("trailer plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer plan"} $code"

    fun measuredValue(): Double = loadKg

    fun snapshot(): TrailerPlan {
        return TrailerPlan(
            code = code,
            title = title,
            trailerId = trailerId,
            externalRef = externalRef,
            loadKg = loadKg,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer plan in status open. */
fun newTrailerPlan(code: String, title: String): TrailerPlan {
    return TrailerPlan(
        code = code,
        title = title,
        trailerId = "",
        externalRef = "",
        loadKg = 0.0,
        reviewer = "",
        status = TrailerPlanStatus.OPEN,
    )
}

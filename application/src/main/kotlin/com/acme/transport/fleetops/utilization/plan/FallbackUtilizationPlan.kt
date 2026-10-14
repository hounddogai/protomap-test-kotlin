package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback utilization plan records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a fallback utilization plan is in its life cycle. */
enum class FallbackUtilizationPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback utilization plan, with what the planners need to act on it. */
data class FallbackUtilizationPlan(
    var code: String,
    var name: String,
    var title: String,
    var pinned: Boolean,
    var windowDays: Int,
    var budget: Double,
    var revision: Int,
    var lane: String,
    var status: FallbackUtilizationPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackUtilizationPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackUtilizationPlanStatus): Boolean {
        when (status) {
            FallbackUtilizationPlanStatus.DRAFT -> {
                when (next) {
                    FallbackUtilizationPlanStatus.SUSPENDED, FallbackUtilizationPlanStatus.CLOSED -> return true
                }
                return false
            }
            FallbackUtilizationPlanStatus.PENDING -> return next == FallbackUtilizationPlanStatus.CLOSED
            FallbackUtilizationPlanStatus.ACTIVE -> return next == FallbackUtilizationPlanStatus.SUSPENDED || next == FallbackUtilizationPlanStatus.CLOSED
            FallbackUtilizationPlanStatus.SUSPENDED -> return next == FallbackUtilizationPlanStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FallbackUtilizationPlanStatus) {
        if (!canMoveTo(next)) {
            error("fallback utilization plan $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((windowDays.toDouble() - budget) * 100) / 100

    fun snapshot(): FallbackUtilizationPlan {
        return FallbackUtilizationPlan(
            code = code,
            name = name,
            title = title,
            pinned = pinned,
            windowDays = windowDays,
            budget = budget,
            revision = revision,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback utilization plan in status draft. */
fun newFallbackUtilizationPlan(code: String, name: String): FallbackUtilizationPlan {
    return FallbackUtilizationPlan(
        code = code,
        name = name,
        title = "",
        pinned = false,
        windowDays = 0,
        budget = 0.0,
        revision = 0,
        lane = "",
        status = FallbackUtilizationPlanStatus.DRAFT,
    )
}

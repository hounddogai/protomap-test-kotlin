package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive swap budget is in its life cycle. */
enum class AdaptiveSwapBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive swap budget and its current status. */
data class AdaptiveSwapBudget(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var driverId: String,
    var cost: Double,
    var attempts: Int,
    var summary: String,
    var status: AdaptiveSwapBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapBudgetStatus): Boolean {
        when (status) {
            AdaptiveSwapBudgetStatus.OPEN -> {
                when (next) {
                    AdaptiveSwapBudgetStatus.IN_REVIEW, AdaptiveSwapBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveSwapBudgetStatus.IN_REVIEW -> return next == AdaptiveSwapBudgetStatus.REOPENED || next == AdaptiveSwapBudgetStatus.ARCHIVED
            AdaptiveSwapBudgetStatus.RESOLVED -> {
                when (next) {
                    AdaptiveSwapBudgetStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveSwapBudgetStatus.REOPENED -> return next == AdaptiveSwapBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapBudgetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive swap budget"} $code"

    fun projectedTotal(): Double = round((cost + attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveSwapBudget {
        return AdaptiveSwapBudget(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            driverId = driverId,
            cost = cost,
            attempts = attempts,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive swap budget in status open. */
fun newAdaptiveSwapBudget(code: String, title: String): AdaptiveSwapBudget {
    return AdaptiveSwapBudget(
        code = code,
        title = title,
        durationMinutes = 0,
        driverId = "",
        cost = 0.0,
        attempts = 0,
        summary = "",
        status = AdaptiveSwapBudgetStatus.OPEN,
    )
}

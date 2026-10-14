package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal coaching budget is in its life cycle. */
enum class SeasonalCoachingBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One seasonal coaching budget, with what the planners need to act on it. */
data class SeasonalCoachingBudget(
    var code: String,
    var label: String,
    var priority: Int,
    var margin: Double,
    var ownerId: String,
    var graceDays: Int,
    var status: SeasonalCoachingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCoachingBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCoachingBudgetStatus): Boolean {
        when (status) {
            SeasonalCoachingBudgetStatus.NEW -> return next == SeasonalCoachingBudgetStatus.RUNNING || next == SeasonalCoachingBudgetStatus.FAILED
            SeasonalCoachingBudgetStatus.QUEUED -> return next == SeasonalCoachingBudgetStatus.RUNNING
            SeasonalCoachingBudgetStatus.RUNNING -> return next == SeasonalCoachingBudgetStatus.DONE || next == SeasonalCoachingBudgetStatus.FAILED
            SeasonalCoachingBudgetStatus.DONE -> {
                when (next) {
                    SeasonalCoachingBudgetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCoachingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal coaching budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((priority.toDouble() - margin) * 100) / 100

    fun snapshot(): SeasonalCoachingBudget {
        return SeasonalCoachingBudget(
            code = code,
            label = label,
            priority = priority,
            margin = margin,
            ownerId = ownerId,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal coaching budget in status new. */
fun newSeasonalCoachingBudget(code: String, label: String): SeasonalCoachingBudget {
    return SeasonalCoachingBudget(
        code = code,
        label = label,
        priority = 0,
        margin = 0.0,
        ownerId = "",
        graceDays = 0,
        status = SeasonalCoachingBudgetStatus.NEW,
    )
}

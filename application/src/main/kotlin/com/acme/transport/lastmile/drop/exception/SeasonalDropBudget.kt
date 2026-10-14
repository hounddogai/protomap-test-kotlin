package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal drop budget is in its life cycle. */
enum class SeasonalDropBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal drop budget and its current status. */
data class SeasonalDropBudget(
    var code: String,
    var title: String,
    var margin: Double,
    var currency: String,
    var windowDays: Int,
    var status: SeasonalDropBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDropBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDropBudgetStatus): Boolean {
        when (status) {
            SeasonalDropBudgetStatus.OPEN -> {
                when (next) {
                    SeasonalDropBudgetStatus.RESOLVED -> return true
                }
                return false
            }
            SeasonalDropBudgetStatus.IN_REVIEW -> return next == SeasonalDropBudgetStatus.RESOLVED || next == SeasonalDropBudgetStatus.REOPENED
            SeasonalDropBudgetStatus.RESOLVED -> {
                when (next) {
                    SeasonalDropBudgetStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalDropBudgetStatus.REOPENED -> {
                when (next) {
                    SeasonalDropBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDropBudgetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal drop budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((margin - windowDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDropBudget {
        return SeasonalDropBudget(
            code = code,
            title = title,
            margin = margin,
            currency = currency,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal drop budget in status open. */
fun newSeasonalDropBudget(code: String, title: String): SeasonalDropBudget {
    return SeasonalDropBudget(
        code = code,
        title = title,
        margin = 0.0,
        currency = "",
        windowDays = 0,
        status = SeasonalDropBudgetStatus.OPEN,
    )
}

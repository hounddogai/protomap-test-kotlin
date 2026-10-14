package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global scorecard budget is in its life cycle. */
enum class GlobalScorecardBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global scorecard budget and its current status. */
data class GlobalScorecardBudget(
    var code: String,
    var name: String,
    var revision: Int,
    var reviewer: String,
    var locale: String,
    var channel: String,
    var amount: Double,
    var approved: Boolean,
    var trailerId: String,
    var status: GlobalScorecardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalScorecardBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalScorecardBudgetStatus): Boolean {
        when (status) {
            GlobalScorecardBudgetStatus.OPEN -> return next == GlobalScorecardBudgetStatus.IN_REVIEW || next == GlobalScorecardBudgetStatus.RESOLVED
            GlobalScorecardBudgetStatus.IN_REVIEW -> {
                when (next) {
                    GlobalScorecardBudgetStatus.RESOLVED, GlobalScorecardBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalScorecardBudgetStatus.RESOLVED -> {
                when (next) {
                    GlobalScorecardBudgetStatus.REOPENED -> return true
                }
                return false
            }
            GlobalScorecardBudgetStatus.REOPENED -> return next == GlobalScorecardBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalScorecardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("global scorecard budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((amount - revision.toDouble()) * 100) / 100

    fun snapshot(): GlobalScorecardBudget {
        return GlobalScorecardBudget(
            code = code,
            name = name,
            revision = revision,
            reviewer = reviewer,
            locale = locale,
            channel = channel,
            amount = amount,
            approved = approved,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global scorecard budget in status open. */
fun newGlobalScorecardBudget(code: String, name: String): GlobalScorecardBudget {
    return GlobalScorecardBudget(
        code = code,
        name = name,
        revision = 0,
        reviewer = "",
        locale = "",
        channel = "",
        amount = 0.0,
        approved = false,
        trailerId = "",
        status = GlobalScorecardBudgetStatus.OPEN,
    )
}

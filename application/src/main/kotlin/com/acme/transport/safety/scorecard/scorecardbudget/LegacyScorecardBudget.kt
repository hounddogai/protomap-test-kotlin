package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy scorecard budget is in its life cycle. */
enum class LegacyScorecardBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy scorecard budget and its current status. */
data class LegacyScorecardBudget(
    var code: String,
    var name: String,
    var ownerId: String,
    var trailerId: String,
    var endsAt: Int,
    var country: String,
    var category: String,
    var distanceKm: Double,
    var amount: Double,
    var version: Int,
    var status: LegacyScorecardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyScorecardBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyScorecardBudgetStatus): Boolean {
        when (status) {
            LegacyScorecardBudgetStatus.PROPOSED -> return next == LegacyScorecardBudgetStatus.REJECTED
            LegacyScorecardBudgetStatus.APPROVED -> {
                when (next) {
                    LegacyScorecardBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyScorecardBudgetStatus.REJECTED -> return next == LegacyScorecardBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyScorecardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("legacy scorecard budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round(endsAt.toDouble() * amount * 100) / 100

    fun snapshot(): LegacyScorecardBudget {
        return LegacyScorecardBudget(
            code = code,
            name = name,
            ownerId = ownerId,
            trailerId = trailerId,
            endsAt = endsAt,
            country = country,
            category = category,
            distanceKm = distanceKm,
            amount = amount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy scorecard budget in status proposed. */
fun newLegacyScorecardBudget(code: String, name: String): LegacyScorecardBudget {
    return LegacyScorecardBudget(
        code = code,
        name = name,
        ownerId = "",
        trailerId = "",
        endsAt = 0,
        country = "",
        category = "",
        distanceKm = 0.0,
        amount = 0.0,
        version = 0,
        status = LegacyScorecardBudgetStatus.PROPOSED,
    )
}

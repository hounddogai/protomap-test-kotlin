package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.normalize

/** Where a seasonal tire budget is in its life cycle. */
enum class SeasonalTireBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal tire budget as the tire team keeps it. */
data class SeasonalTireBudget(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var region: String,
    var trailerId: String,
    var status: SeasonalTireBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTireBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTireBudgetStatus): Boolean {
        when (status) {
            SeasonalTireBudgetStatus.DRAFT -> return next == SeasonalTireBudgetStatus.PENDING
            SeasonalTireBudgetStatus.PENDING -> return next == SeasonalTireBudgetStatus.SUSPENDED
            SeasonalTireBudgetStatus.ACTIVE -> {
                when (next) {
                    SeasonalTireBudgetStatus.SUSPENDED, SeasonalTireBudgetStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalTireBudgetStatus.SUSPENDED -> {
                when (next) {
                    SeasonalTireBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTireBudgetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tire budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): SeasonalTireBudget {
        return SeasonalTireBudget(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            region = region,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal tire budget in status draft. */
fun newSeasonalTireBudget(code: String, name: String): SeasonalTireBudget {
    return SeasonalTireBudget(
        code = code,
        name = name,
        etaMinutes = 0,
        region = "",
        trailerId = "",
        status = SeasonalTireBudgetStatus.DRAFT,
    )
}

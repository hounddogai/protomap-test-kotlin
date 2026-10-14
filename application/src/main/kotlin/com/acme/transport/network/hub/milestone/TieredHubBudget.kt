package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered hub budget is in its life cycle. */
enum class TieredHubBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered hub budget and its current status. */
data class TieredHubBudget(
    var code: String,
    var title: String,
    var label: String,
    var odometer: Int,
    var dock: String,
    var shiftCode: String,
    var budget: Double,
    var retries: Int,
    var amount: Double,
    var status: TieredHubBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredHubBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredHubBudgetStatus): Boolean {
        when (status) {
            TieredHubBudgetStatus.PROPOSED -> {
                when (next) {
                    TieredHubBudgetStatus.REJECTED, TieredHubBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredHubBudgetStatus.APPROVED -> return next == TieredHubBudgetStatus.REJECTED
            TieredHubBudgetStatus.REJECTED -> return next == TieredHubBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredHubBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered hub budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun adjustedVolume(): Double = round((amount - budget) * 100) / 100

    fun snapshot(): TieredHubBudget {
        return TieredHubBudget(
            code = code,
            title = title,
            label = label,
            odometer = odometer,
            dock = dock,
            shiftCode = shiftCode,
            budget = budget,
            retries = retries,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered hub budget in status proposed. */
fun newTieredHubBudget(code: String, title: String): TieredHubBudget {
    return TieredHubBudget(
        code = code,
        title = title,
        label = "",
        odometer = 0,
        dock = "",
        shiftCode = "",
        budget = 0.0,
        retries = 0,
        amount = 0.0,
        status = TieredHubBudgetStatus.PROPOSED,
    )
}

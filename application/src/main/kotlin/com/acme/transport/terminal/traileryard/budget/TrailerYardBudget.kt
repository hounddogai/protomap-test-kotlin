package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard budget records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard budget is in its life cycle. */
enum class TrailerYardBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A trailer yard budget as the trailer yard team keeps it. */
data class TrailerYardBudget(
    var code: String,
    var name: String,
    var floorValue: Double,
    var maximum: Double,
    var country: String,
    var region: String,
    var active: Boolean,
    var status: TrailerYardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardBudgetStatus): Boolean {
        when (status) {
            TrailerYardBudgetStatus.DRAFT -> {
                when (next) {
                    TrailerYardBudgetStatus.PENDING, TrailerYardBudgetStatus.ACTIVE -> return true
                }
                return false
            }
            TrailerYardBudgetStatus.PENDING -> return next == TrailerYardBudgetStatus.ACTIVE
            TrailerYardBudgetStatus.ACTIVE -> {
                when (next) {
                    TrailerYardBudgetStatus.SUSPENDED, TrailerYardBudgetStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardBudgetStatus.SUSPENDED -> {
                when (next) {
                    TrailerYardBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((maximum - floorValue) * 100) / 100

    fun snapshot(): TrailerYardBudget {
        return TrailerYardBudget(
            code = code,
            name = name,
            floorValue = floorValue,
            maximum = maximum,
            country = country,
            region = region,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a trailer yard budget in status draft. */
fun newTrailerYardBudget(code: String, name: String): TrailerYardBudget {
    return TrailerYardBudget(
        code = code,
        name = name,
        floorValue = 0.0,
        maximum = 0.0,
        country = "",
        region = "",
        active = false,
        status = TrailerYardBudgetStatus.DRAFT,
    )
}

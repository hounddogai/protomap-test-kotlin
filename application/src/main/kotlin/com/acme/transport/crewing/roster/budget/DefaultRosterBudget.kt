package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default roster budget records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a default roster budget is in its life cycle. */
enum class DefaultRosterBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default roster budget, with what the planners need to act on it. */
data class DefaultRosterBudget(
    var code: String,
    var title: String,
    var revision: Int,
    var budget: Double,
    var score: Double,
    var dock: String,
    var segment: String,
    var category: String,
    var odometer: Int,
    var weight: Double,
    var floorValue: Double,
    var status: DefaultRosterBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterBudgetStatus): Boolean {
        when (status) {
            DefaultRosterBudgetStatus.CREATED -> return next == DefaultRosterBudgetStatus.VALIDATED || next == DefaultRosterBudgetStatus.PUBLISHED
            DefaultRosterBudgetStatus.VALIDATED -> {
                when (next) {
                    DefaultRosterBudgetStatus.PUBLISHED, DefaultRosterBudgetStatus.RETIRED -> return true
                }
                return false
            }
            DefaultRosterBudgetStatus.PUBLISHED -> return next == DefaultRosterBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultRosterBudgetStatus) {
        if (!canMoveTo(next)) {
            error("default roster budget $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round((weight - score) * 100) / 100

    fun snapshot(): DefaultRosterBudget {
        return DefaultRosterBudget(
            code = code,
            title = title,
            revision = revision,
            budget = budget,
            score = score,
            dock = dock,
            segment = segment,
            category = category,
            odometer = odometer,
            weight = weight,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default roster budget in status created. */
fun newDefaultRosterBudget(code: String, title: String): DefaultRosterBudget {
    return DefaultRosterBudget(
        code = code,
        title = title,
        revision = 0,
        budget = 0.0,
        score = 0.0,
        dock = "",
        segment = "",
        category = "",
        odometer = 0,
        weight = 0.0,
        floorValue = 0.0,
        status = DefaultRosterBudgetStatus.CREATED,
    )
}

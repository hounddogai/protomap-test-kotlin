package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default dwell budget is in its life cycle. */
enum class DefaultDwellBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default dwell budget, with what the planners need to act on it. */
data class DefaultDwellBudget(
    var code: String,
    var title: String,
    var weight: Double,
    var budget: Double,
    var active: Boolean,
    var approved: Boolean,
    var reviewer: String,
    var minimum: Double,
    var status: DefaultDwellBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDwellBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDwellBudgetStatus): Boolean {
        when (status) {
            DefaultDwellBudgetStatus.IDLE -> return next == DefaultDwellBudgetStatus.WARMING || next == DefaultDwellBudgetStatus.STOPPED
            DefaultDwellBudgetStatus.WARMING -> return next == DefaultDwellBudgetStatus.READY || next == DefaultDwellBudgetStatus.DRAINING
            DefaultDwellBudgetStatus.READY -> {
                when (next) {
                    DefaultDwellBudgetStatus.STOPPED -> return true
                }
                return false
            }
            DefaultDwellBudgetStatus.DRAINING -> return next == DefaultDwellBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultDwellBudgetStatus) {
        if (!canMoveTo(next)) {
            error("default dwell budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((budget - minimum) * 100) / 100

    fun snapshot(): DefaultDwellBudget {
        return DefaultDwellBudget(
            code = code,
            title = title,
            weight = weight,
            budget = budget,
            active = active,
            approved = approved,
            reviewer = reviewer,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default dwell budget in status idle. */
fun newDefaultDwellBudget(code: String, title: String): DefaultDwellBudget {
    return DefaultDwellBudget(
        code = code,
        title = title,
        weight = 0.0,
        budget = 0.0,
        active = false,
        approved = false,
        reviewer = "",
        minimum = 0.0,
        status = DefaultDwellBudgetStatus.IDLE,
    )
}

package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled rest break budget is in its life cycle. */
enum class ScheduledRestBreakBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled rest break budget. Its code is unique within a tenant. */
data class ScheduledRestBreakBudget(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var retries: Int,
    var windowDays: Int,
    var approved: Boolean,
    var status: ScheduledRestBreakBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakBudgetStatus): Boolean {
        when (status) {
            ScheduledRestBreakBudgetStatus.DRAFT -> {
                when (next) {
                    ScheduledRestBreakBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledRestBreakBudgetStatus.PENDING -> {
                when (next) {
                    ScheduledRestBreakBudgetStatus.ACTIVE, ScheduledRestBreakBudgetStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRestBreakBudgetStatus.ACTIVE -> {
                when (next) {
                    ScheduledRestBreakBudgetStatus.SUSPENDED, ScheduledRestBreakBudgetStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRestBreakBudgetStatus.SUSPENDED -> {
                when (next) {
                    ScheduledRestBreakBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((fuelLiters - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRestBreakBudget {
        return ScheduledRestBreakBudget(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            retries = retries,
            windowDays = windowDays,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled rest break budget in status draft. */
fun newScheduledRestBreakBudget(code: String, title: String): ScheduledRestBreakBudget {
    return ScheduledRestBreakBudget(
        code = code,
        title = title,
        fuelLiters = 0.0,
        retries = 0,
        windowDays = 0,
        approved = false,
        status = ScheduledRestBreakBudgetStatus.DRAFT,
    )
}

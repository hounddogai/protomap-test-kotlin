package com.acme.transport.crewing.roster.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster budget is in its life cycle. */
enum class RosterBudgetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A roster budget. Its code is unique within a tenant. */
data class RosterBudget(
    var code: String,
    var title: String,
    var score: Double,
    var active: Boolean,
    var priority: Int,
    var budget: Double,
    var startsAt: Int,
    var shiftCode: String,
    var region: String,
    var status: RosterBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterBudgetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterBudgetStatus): Boolean {
        when (status) {
            RosterBudgetStatus.PLANNED -> {
                when (next) {
                    RosterBudgetStatus.SCHEDULED, RosterBudgetStatus.CANCELLED -> return true
                }
                return false
            }
            RosterBudgetStatus.SCHEDULED -> return next == RosterBudgetStatus.IN_PROGRESS
            RosterBudgetStatus.IN_PROGRESS -> {
                when (next) {
                    RosterBudgetStatus.COMPLETED -> return true
                }
                return false
            }
            RosterBudgetStatus.COMPLETED -> {
                when (next) {
                    RosterBudgetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterBudgetStatus) {
        if (!canMoveTo(next)) {
            error("roster budget $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round((startsAt.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): RosterBudget {
        return RosterBudget(
            code = code,
            title = title,
            score = score,
            active = active,
            priority = priority,
            budget = budget,
            startsAt = startsAt,
            shiftCode = shiftCode,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a roster budget in status planned. */
fun newRosterBudget(code: String, title: String): RosterBudget {
    return RosterBudget(
        code = code,
        title = title,
        score = 0.0,
        active = false,
        priority = 0,
        budget = 0.0,
        startsAt = 0,
        shiftCode = "",
        region = "",
        status = RosterBudgetStatus.PLANNED,
    )
}

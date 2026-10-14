package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.text.normalize

/** Where a default coaching budget is in its life cycle. */
enum class DefaultCoachingBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One default coaching budget, with what the planners need to act on it. */
data class DefaultCoachingBudget(
    var code: String,
    var label: String,
    var channel: String,
    var rate: Double,
    var pinned: Boolean,
    var source: String,
    var status: DefaultCoachingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingBudgetStatus): Boolean {
        when (status) {
            DefaultCoachingBudgetStatus.IDLE -> return next == DefaultCoachingBudgetStatus.WARMING || next == DefaultCoachingBudgetStatus.READY
            DefaultCoachingBudgetStatus.WARMING -> {
                when (next) {
                    DefaultCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
            DefaultCoachingBudgetStatus.READY -> {
                when (next) {
                    DefaultCoachingBudgetStatus.DRAINING, DefaultCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
            DefaultCoachingBudgetStatus.DRAINING -> {
                when (next) {
                    DefaultCoachingBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCoachingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("default coaching budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = rate

    fun snapshot(): DefaultCoachingBudget {
        return DefaultCoachingBudget(
            code = code,
            label = label,
            channel = channel,
            rate = rate,
            pinned = pinned,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default coaching budget in status idle. */
fun newDefaultCoachingBudget(code: String, label: String): DefaultCoachingBudget {
    return DefaultCoachingBudget(
        code = code,
        label = label,
        channel = "",
        rate = 0.0,
        pinned = false,
        source = "",
        status = DefaultCoachingBudgetStatus.IDLE,
    )
}

package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient transit time budget records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a lenient transit time budget is in its life cycle. */
enum class LenientTransitTimeBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient transit time budget, with what the planners need to act on it. */
data class LenientTransitTimeBudget(
    var code: String,
    var title: String,
    var minimum: Double,
    var name: String,
    var sequence: Int,
    var threshold: Double,
    var status: LenientTransitTimeBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTransitTimeBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTransitTimeBudgetStatus): Boolean {
        when (status) {
            LenientTransitTimeBudgetStatus.DRAFT -> return next == LenientTransitTimeBudgetStatus.PENDING || next == LenientTransitTimeBudgetStatus.CLOSED
            LenientTransitTimeBudgetStatus.PENDING -> return next == LenientTransitTimeBudgetStatus.ACTIVE
            LenientTransitTimeBudgetStatus.ACTIVE -> {
                when (next) {
                    LenientTransitTimeBudgetStatus.SUSPENDED, LenientTransitTimeBudgetStatus.CLOSED -> return true
                }
                return false
            }
            LenientTransitTimeBudgetStatus.SUSPENDED -> return next == LenientTransitTimeBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientTransitTimeBudgetStatus) {
        if (!canMoveTo(next)) {
            error("lenient transit time budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((threshold - sequence.toDouble()) * 100) / 100

    fun snapshot(): LenientTransitTimeBudget {
        return LenientTransitTimeBudget(
            code = code,
            title = title,
            minimum = minimum,
            name = name,
            sequence = sequence,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient transit time budget in status draft. */
fun newLenientTransitTimeBudget(code: String, title: String): LenientTransitTimeBudget {
    return LenientTransitTimeBudget(
        code = code,
        title = title,
        minimum = 0.0,
        name = "",
        sequence = 0,
        threshold = 0.0,
        status = LenientTransitTimeBudgetStatus.DRAFT,
    )
}

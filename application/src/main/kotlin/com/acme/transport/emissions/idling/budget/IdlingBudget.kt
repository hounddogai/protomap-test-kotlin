package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling budget records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling budget is in its life cycle. */
enum class IdlingBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A idling budget as the idling team keeps it. */
data class IdlingBudget(
    var code: String,
    var label: String,
    var score: Double,
    var version: Int,
    var threshold: Double,
    var lane: String,
    var depot: String,
    var summary: String,
    var odometer: Int,
    var status: IdlingBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingBudgetStatus): Boolean {
        when (status) {
            IdlingBudgetStatus.NEW -> {
                when (next) {
                    IdlingBudgetStatus.DONE -> return true
                }
                return false
            }
            IdlingBudgetStatus.QUEUED -> return next == IdlingBudgetStatus.DONE || next == IdlingBudgetStatus.FAILED
            IdlingBudgetStatus.RUNNING -> {
                when (next) {
                    IdlingBudgetStatus.DONE, IdlingBudgetStatus.FAILED -> return true
                }
                return false
            }
            IdlingBudgetStatus.DONE -> return next == IdlingBudgetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: IdlingBudgetStatus) {
        if (!canMoveTo(next)) {
            error("idling budget $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"idling budget"} $code"

    fun netVolume(): Double = round((odometer.toDouble() - score) * 100) / 100

    fun snapshot(): IdlingBudget {
        return IdlingBudget(
            code = code,
            label = label,
            score = score,
            version = version,
            threshold = threshold,
            lane = lane,
            depot = depot,
            summary = summary,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a idling budget in status new. */
fun newIdlingBudget(code: String, label: String): IdlingBudget {
    return IdlingBudget(
        code = code,
        label = label,
        score = 0.0,
        version = 0,
        threshold = 0.0,
        lane = "",
        depot = "",
        summary = "",
        odometer = 0,
        status = IdlingBudgetStatus.NEW,
    )
}

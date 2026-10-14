package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional camera event budget records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a regional camera event budget is in its life cycle. */
enum class RegionalCameraEventBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional camera event budget, with what the planners need to act on it. */
data class RegionalCameraEventBudget(
    var code: String,
    var title: String,
    var capacity: Int,
    var quantity: Int,
    var channel: String,
    var active: Boolean,
    var rate: Double,
    var attempts: Int,
    var retries: Int,
    var notes: String,
    var budget: Double,
    var status: RegionalCameraEventBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCameraEventBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCameraEventBudgetStatus): Boolean {
        when (status) {
            RegionalCameraEventBudgetStatus.NEW -> return next == RegionalCameraEventBudgetStatus.DONE
            RegionalCameraEventBudgetStatus.QUEUED -> return next == RegionalCameraEventBudgetStatus.FAILED
            RegionalCameraEventBudgetStatus.RUNNING -> {
                when (next) {
                    RegionalCameraEventBudgetStatus.FAILED -> return true
                }
                return false
            }
            RegionalCameraEventBudgetStatus.DONE -> return next == RegionalCameraEventBudgetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalCameraEventBudgetStatus) {
        if (!canMoveTo(next)) {
            error("regional camera event budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional camera event budget"} $code"

    fun effectiveTotal(): Double = round((rate + attempts.toDouble()) * 100) / 100

    fun snapshot(): RegionalCameraEventBudget {
        return RegionalCameraEventBudget(
            code = code,
            title = title,
            capacity = capacity,
            quantity = quantity,
            channel = channel,
            active = active,
            rate = rate,
            attempts = attempts,
            retries = retries,
            notes = notes,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional camera event budget in status new. */
fun newRegionalCameraEventBudget(code: String, title: String): RegionalCameraEventBudget {
    return RegionalCameraEventBudget(
        code = code,
        title = title,
        capacity = 0,
        quantity = 0,
        channel = "",
        active = false,
        rate = 0.0,
        attempts = 0,
        retries = 0,
        notes = "",
        budget = 0.0,
        status = RegionalCameraEventBudgetStatus.NEW,
    )
}

package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery budget is in its life cycle. */
enum class RedeliveryBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a redelivery budget and its current status. */
data class RedeliveryBudget(
    var code: String,
    var title: String,
    var sequence: Int,
    var windowDays: Int,
    var floorValue: Double,
    var cost: Double,
    var pallets: Int,
    var status: RedeliveryBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryBudgetStatus): Boolean {
        when (status) {
            RedeliveryBudgetStatus.DRAFT -> {
                when (next) {
                    RedeliveryBudgetStatus.PENDING, RedeliveryBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            RedeliveryBudgetStatus.PENDING -> return next == RedeliveryBudgetStatus.ACTIVE
            RedeliveryBudgetStatus.ACTIVE -> return next == RedeliveryBudgetStatus.CLOSED
            RedeliveryBudgetStatus.SUSPENDED -> {
                when (next) {
                    RedeliveryBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryBudgetStatus) {
        if (!canMoveTo(next)) {
            error("redelivery budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((sequence.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryBudget {
        return RedeliveryBudget(
            code = code,
            title = title,
            sequence = sequence,
            windowDays = windowDays,
            floorValue = floorValue,
            cost = cost,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a redelivery budget in status draft. */
fun newRedeliveryBudget(code: String, title: String): RedeliveryBudget {
    return RedeliveryBudget(
        code = code,
        title = title,
        sequence = 0,
        windowDays = 0,
        floorValue = 0.0,
        cost = 0.0,
        pallets = 0,
        status = RedeliveryBudgetStatus.DRAFT,
    )
}

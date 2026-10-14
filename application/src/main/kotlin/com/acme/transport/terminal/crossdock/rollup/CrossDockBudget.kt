package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock budget is in its life cycle. */
enum class CrossDockBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A cross dock budget as the cross dock team keeps it. */
data class CrossDockBudget(
    var code: String,
    var name: String,
    var source: String,
    var attempts: Int,
    var capacity: Int,
    var pinned: Boolean,
    var status: CrossDockBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockBudgetStatus): Boolean {
        when (status) {
            CrossDockBudgetStatus.PROPOSED -> {
                when (next) {
                    CrossDockBudgetStatus.REJECTED, CrossDockBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            CrossDockBudgetStatus.APPROVED -> {
                when (next) {
                    CrossDockBudgetStatus.REJECTED, CrossDockBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            CrossDockBudgetStatus.REJECTED -> {
                when (next) {
                    CrossDockBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockBudgetStatus) {
        if (!canMoveTo(next)) {
            error("cross dock budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round((capacity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): CrossDockBudget {
        return CrossDockBudget(
            code = code,
            name = name,
            source = source,
            attempts = attempts,
            capacity = capacity,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cross dock budget in status proposed. */
fun newCrossDockBudget(code: String, name: String): CrossDockBudget {
    return CrossDockBudget(
        code = code,
        name = name,
        source = "",
        attempts = 0,
        capacity = 0,
        pinned = false,
        status = CrossDockBudgetStatus.PROPOSED,
    )
}

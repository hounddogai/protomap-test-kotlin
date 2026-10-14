package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.text.normalize

/** Where a drop budget is in its life cycle. */
enum class DropBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A drop budget. Its code is unique within a tenant. */
data class DropBudget(
    var code: String,
    var name: String,
    var visible: Boolean,
    var country: String,
    var margin: Double,
    var summary: String,
    var status: DropBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropBudgetStatus): Boolean {
        when (status) {
            DropBudgetStatus.DRAFT -> {
                when (next) {
                    DropBudgetStatus.CLOSED -> return true
                }
                return false
            }
            DropBudgetStatus.PENDING -> return next == DropBudgetStatus.SUSPENDED || next == DropBudgetStatus.CLOSED
            DropBudgetStatus.ACTIVE -> {
                when (next) {
                    DropBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            DropBudgetStatus.SUSPENDED -> return next == DropBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DropBudgetStatus) {
        if (!canMoveTo(next)) {
            error("drop budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"drop budget"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): DropBudget {
        return DropBudget(
            code = code,
            name = name,
            visible = visible,
            country = country,
            margin = margin,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a drop budget in status draft. */
fun newDropBudget(code: String, name: String): DropBudget {
    return DropBudget(
        code = code,
        name = name,
        visible = false,
        country = "",
        margin = 0.0,
        summary = "",
        status = DropBudgetStatus.DRAFT,
    )
}

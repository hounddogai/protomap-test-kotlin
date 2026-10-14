package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending near miss budget is in its life cycle. */
enum class PendingNearMissBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending near miss budget as the near miss team keeps it. */
data class PendingNearMissBudget(
    var code: String,
    var title: String,
    var graceDays: Int,
    var expiresAt: Int,
    var routeCode: String,
    var trailerId: String,
    var budget: Double,
    var batchSize: Int,
    var status: PendingNearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNearMissBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNearMissBudgetStatus): Boolean {
        when (status) {
            PendingNearMissBudgetStatus.DRAFT -> {
                when (next) {
                    PendingNearMissBudgetStatus.PENDING, PendingNearMissBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingNearMissBudgetStatus.PENDING -> return next == PendingNearMissBudgetStatus.SUSPENDED || next == PendingNearMissBudgetStatus.CLOSED
            PendingNearMissBudgetStatus.ACTIVE -> {
                when (next) {
                    PendingNearMissBudgetStatus.SUSPENDED, PendingNearMissBudgetStatus.CLOSED -> return true
                }
                return false
            }
            PendingNearMissBudgetStatus.SUSPENDED -> {
                when (next) {
                    PendingNearMissBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("pending near miss budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round((batchSize.toDouble() + budget) * 100) / 100

    fun snapshot(): PendingNearMissBudget {
        return PendingNearMissBudget(
            code = code,
            title = title,
            graceDays = graceDays,
            expiresAt = expiresAt,
            routeCode = routeCode,
            trailerId = trailerId,
            budget = budget,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending near miss budget in status draft. */
fun newPendingNearMissBudget(code: String, title: String): PendingNearMissBudget {
    return PendingNearMissBudget(
        code = code,
        title = title,
        graceDays = 0,
        expiresAt = 0,
        routeCode = "",
        trailerId = "",
        budget = 0.0,
        batchSize = 0,
        status = PendingNearMissBudgetStatus.DRAFT,
    )
}

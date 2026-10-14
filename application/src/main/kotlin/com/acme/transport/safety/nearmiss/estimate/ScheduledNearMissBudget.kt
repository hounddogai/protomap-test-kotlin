package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled near miss budget is in its life cycle. */
enum class ScheduledNearMissBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled near miss budget as the near miss team keeps it. */
data class ScheduledNearMissBudget(
    var code: String,
    var title: String,
    var category: String,
    var createdAt: Int,
    var etaMinutes: Int,
    var amount: Double,
    var status: ScheduledNearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissBudgetStatus): Boolean {
        when (status) {
            ScheduledNearMissBudgetStatus.OPEN -> {
                when (next) {
                    ScheduledNearMissBudgetStatus.IN_REVIEW, ScheduledNearMissBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledNearMissBudgetStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledNearMissBudgetStatus.RESOLVED, ScheduledNearMissBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledNearMissBudgetStatus.RESOLVED -> return next == ScheduledNearMissBudgetStatus.REOPENED || next == ScheduledNearMissBudgetStatus.ARCHIVED
            ScheduledNearMissBudgetStatus.REOPENED -> {
                when (next) {
                    ScheduledNearMissBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netTotal(): Double = round(createdAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ScheduledNearMissBudget {
        return ScheduledNearMissBudget(
            code = code,
            title = title,
            category = category,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled near miss budget in status open. */
fun newScheduledNearMissBudget(code: String, title: String): ScheduledNearMissBudget {
    return ScheduledNearMissBudget(
        code = code,
        title = title,
        category = "",
        createdAt = 0,
        etaMinutes = 0,
        amount = 0.0,
        status = ScheduledNearMissBudgetStatus.OPEN,
    )
}

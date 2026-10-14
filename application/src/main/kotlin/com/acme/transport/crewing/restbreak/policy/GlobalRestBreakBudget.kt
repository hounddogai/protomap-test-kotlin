package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rest break budget is in its life cycle. */
enum class GlobalRestBreakBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global rest break budget. Its code is unique within a tenant. */
data class GlobalRestBreakBudget(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var stopCount: Int,
    var distanceKm: Double,
    var description: String,
    var status: GlobalRestBreakBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRestBreakBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRestBreakBudgetStatus): Boolean {
        when (status) {
            GlobalRestBreakBudgetStatus.OPEN -> {
                when (next) {
                    GlobalRestBreakBudgetStatus.IN_REVIEW, GlobalRestBreakBudgetStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalRestBreakBudgetStatus.IN_REVIEW -> {
                when (next) {
                    GlobalRestBreakBudgetStatus.RESOLVED, GlobalRestBreakBudgetStatus.REOPENED -> return true
                }
                return false
            }
            GlobalRestBreakBudgetStatus.RESOLVED -> return next == GlobalRestBreakBudgetStatus.REOPENED || next == GlobalRestBreakBudgetStatus.ARCHIVED
            GlobalRestBreakBudgetStatus.REOPENED -> return next == GlobalRestBreakBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalRestBreakBudgetStatus) {
        if (!canMoveTo(next)) {
            error("global rest break budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((stopCount.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): GlobalRestBreakBudget {
        return GlobalRestBreakBudget(
            code = code,
            label = label,
            tags = tags,
            updatedAt = updatedAt,
            stopCount = stopCount,
            distanceKm = distanceKm,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global rest break budget in status open. */
fun newGlobalRestBreakBudget(code: String, label: String): GlobalRestBreakBudget {
    return GlobalRestBreakBudget(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        updatedAt = 0,
        stopCount = 0,
        distanceKm = 0.0,
        description = "",
        status = GlobalRestBreakBudgetStatus.OPEN,
    )
}

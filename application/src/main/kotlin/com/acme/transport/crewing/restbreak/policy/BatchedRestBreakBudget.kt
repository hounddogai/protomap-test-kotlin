package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize

/** Where a batched rest break budget is in its life cycle. */
enum class BatchedRestBreakBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched rest break budget and its current status. */
data class BatchedRestBreakBudget(
    var code: String,
    var name: String,
    var phone: String,
    var category: String,
    var attributes: MutableMap<String, String>,
    var ratio: Double,
    var status: BatchedRestBreakBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRestBreakBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRestBreakBudgetStatus): Boolean {
        when (status) {
            BatchedRestBreakBudgetStatus.PROPOSED -> {
                when (next) {
                    BatchedRestBreakBudgetStatus.REJECTED, BatchedRestBreakBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedRestBreakBudgetStatus.APPROVED -> return next == BatchedRestBreakBudgetStatus.WITHDRAWN
            BatchedRestBreakBudgetStatus.REJECTED -> return next == BatchedRestBreakBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedRestBreakBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched rest break budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = ratio

    fun snapshot(): BatchedRestBreakBudget {
        return BatchedRestBreakBudget(
            code = code,
            name = name,
            phone = phone,
            category = category,
            attributes = attributes,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched rest break budget in status proposed. */
fun newBatchedRestBreakBudget(code: String, name: String): BatchedRestBreakBudget {
    return BatchedRestBreakBudget(
        code = code,
        name = name,
        phone = "",
        category = "",
        attributes = mutableMapOf<String, String>(),
        ratio = 0.0,
        status = BatchedRestBreakBudgetStatus.PROPOSED,
    )
}

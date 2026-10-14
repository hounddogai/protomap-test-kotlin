package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.normalize

/** Where a batched drop budget is in its life cycle. */
enum class BatchedDropBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a batched drop budget and its current status. */
data class BatchedDropBudget(
    var code: String,
    var label: String,
    var summary: String,
    var unit: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var score: Double,
    var status: BatchedDropBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDropBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDropBudgetStatus): Boolean {
        when (status) {
            BatchedDropBudgetStatus.IDLE -> {
                when (next) {
                    BatchedDropBudgetStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDropBudgetStatus.WARMING -> {
                when (next) {
                    BatchedDropBudgetStatus.READY, BatchedDropBudgetStatus.DRAINING -> return true
                }
                return false
            }
            BatchedDropBudgetStatus.READY -> {
                when (next) {
                    BatchedDropBudgetStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDropBudgetStatus.DRAINING -> return next == BatchedDropBudgetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedDropBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched drop budget $code cannot move from $status to $next")
        }
        status = next
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

    fun measuredValue(): Double = score

    fun snapshot(): BatchedDropBudget {
        return BatchedDropBudget(
            code = code,
            label = label,
            summary = summary,
            unit = unit,
            ownerId = ownerId,
            tags = tags,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched drop budget in status idle. */
fun newBatchedDropBudget(code: String, label: String): BatchedDropBudget {
    return BatchedDropBudget(
        code = code,
        label = label,
        summary = "",
        unit = "",
        ownerId = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        status = BatchedDropBudgetStatus.IDLE,
    )
}

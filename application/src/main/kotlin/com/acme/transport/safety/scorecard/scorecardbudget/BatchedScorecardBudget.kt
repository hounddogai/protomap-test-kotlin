package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.normalize

// The batched scorecard budget records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a batched scorecard budget is in its life cycle. */
enum class BatchedScorecardBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched scorecard budget, with what the planners need to act on it. */
data class BatchedScorecardBudget(
    var code: String,
    var title: String,
    var phone: String,
    var checksum: String,
    var discount: Double,
    var status: BatchedScorecardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardBudgetStatus): Boolean {
        when (status) {
            BatchedScorecardBudgetStatus.DRAFT -> return next == BatchedScorecardBudgetStatus.SUSPENDED || next == BatchedScorecardBudgetStatus.CLOSED
            BatchedScorecardBudgetStatus.PENDING -> {
                when (next) {
                    BatchedScorecardBudgetStatus.ACTIVE -> return true
                }
                return false
            }
            BatchedScorecardBudgetStatus.ACTIVE -> {
                when (next) {
                    BatchedScorecardBudgetStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedScorecardBudgetStatus.SUSPENDED -> {
                when (next) {
                    BatchedScorecardBudgetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedScorecardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = discount

    fun snapshot(): BatchedScorecardBudget {
        return BatchedScorecardBudget(
            code = code,
            title = title,
            phone = phone,
            checksum = checksum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched scorecard budget in status draft. */
fun newBatchedScorecardBudget(code: String, title: String): BatchedScorecardBudget {
    return BatchedScorecardBudget(
        code = code,
        title = title,
        phone = "",
        checksum = "",
        discount = 0.0,
        status = BatchedScorecardBudgetStatus.DRAFT,
    )
}

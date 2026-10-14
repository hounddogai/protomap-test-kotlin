package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.text.normalize

/** Where a batched scorecard plan is in its life cycle. */
enum class BatchedScorecardPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched scorecard plan, with what the planners need to act on it. */
data class BatchedScorecardPlan(
    var code: String,
    var title: String,
    var ownerId: String,
    var margin: Double,
    var category: String,
    var status: BatchedScorecardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardPlanStatus): Boolean {
        when (status) {
            BatchedScorecardPlanStatus.CREATED -> return next == BatchedScorecardPlanStatus.VALIDATED
            BatchedScorecardPlanStatus.VALIDATED -> return next == BatchedScorecardPlanStatus.PUBLISHED || next == BatchedScorecardPlanStatus.RETIRED
            BatchedScorecardPlanStatus.PUBLISHED -> return next == BatchedScorecardPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedScorecardPlanStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = margin

    fun snapshot(): BatchedScorecardPlan {
        return BatchedScorecardPlan(
            code = code,
            title = title,
            ownerId = ownerId,
            margin = margin,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched scorecard plan in status created. */
fun newBatchedScorecardPlan(code: String, title: String): BatchedScorecardPlan {
    return BatchedScorecardPlan(
        code = code,
        title = title,
        ownerId = "",
        margin = 0.0,
        category = "",
        status = BatchedScorecardPlanStatus.CREATED,
    )
}

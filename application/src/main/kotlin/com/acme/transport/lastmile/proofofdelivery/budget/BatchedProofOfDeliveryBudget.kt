package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched proof of delivery budget records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a batched proof of delivery budget is in its life cycle. */
enum class BatchedProofOfDeliveryBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched proof of delivery budget as the proof of delivery team keeps it. */
data class BatchedProofOfDeliveryBudget(
    var code: String,
    var label: String,
    var discount: Double,
    var trailerId: String,
    var maximum: Double,
    var priority: Int,
    var version: Int,
    var summary: String,
    var score: Double,
    var weight: Double,
    var status: BatchedProofOfDeliveryBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryBudgetStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryBudgetStatus.OPEN -> return next == BatchedProofOfDeliveryBudgetStatus.IN_REVIEW || next == BatchedProofOfDeliveryBudgetStatus.REOPENED
            BatchedProofOfDeliveryBudgetStatus.IN_REVIEW -> return next == BatchedProofOfDeliveryBudgetStatus.RESOLVED
            BatchedProofOfDeliveryBudgetStatus.RESOLVED -> {
                when (next) {
                    BatchedProofOfDeliveryBudgetStatus.REOPENED, BatchedProofOfDeliveryBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryBudgetStatus.REOPENED -> return next == BatchedProofOfDeliveryBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery budget $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round((version.toDouble() + maximum) * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryBudget {
        return BatchedProofOfDeliveryBudget(
            code = code,
            label = label,
            discount = discount,
            trailerId = trailerId,
            maximum = maximum,
            priority = priority,
            version = version,
            summary = summary,
            score = score,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched proof of delivery budget in status open. */
fun newBatchedProofOfDeliveryBudget(code: String, label: String): BatchedProofOfDeliveryBudget {
    return BatchedProofOfDeliveryBudget(
        code = code,
        label = label,
        discount = 0.0,
        trailerId = "",
        maximum = 0.0,
        priority = 0,
        version = 0,
        summary = "",
        score = 0.0,
        weight = 0.0,
        status = BatchedProofOfDeliveryBudgetStatus.OPEN,
    )
}

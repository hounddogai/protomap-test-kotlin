package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched proof of delivery review is in its life cycle. */
enum class BatchedProofOfDeliveryReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched proof of delivery review as the proof of delivery team keeps it. */
data class BatchedProofOfDeliveryReview(
    var code: String,
    var name: String,
    var carrier: String,
    var score: Double,
    var budget: Double,
    var status: BatchedProofOfDeliveryReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryReviewStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryReviewStatus.PROPOSED -> return next == BatchedProofOfDeliveryReviewStatus.APPROVED || next == BatchedProofOfDeliveryReviewStatus.WITHDRAWN
            BatchedProofOfDeliveryReviewStatus.APPROVED -> {
                when (next) {
                    BatchedProofOfDeliveryReviewStatus.REJECTED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryReviewStatus.REJECTED -> return next == BatchedProofOfDeliveryReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryReviewStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched proof of delivery review"} $code"

    fun adjustedLoad(): Double = round(budget * score * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryReview {
        return BatchedProofOfDeliveryReview(
            code = code,
            name = name,
            carrier = carrier,
            score = score,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched proof of delivery review in status proposed. */
fun newBatchedProofOfDeliveryReview(code: String, name: String): BatchedProofOfDeliveryReview {
    return BatchedProofOfDeliveryReview(
        code = code,
        name = name,
        carrier = "",
        score = 0.0,
        budget = 0.0,
        status = BatchedProofOfDeliveryReviewStatus.PROPOSED,
    )
}

package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched proof of delivery policy is in its life cycle. */
enum class BatchedProofOfDeliveryPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched proof of delivery policy and its current status. */
data class BatchedProofOfDeliveryPolicy(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var approved: Boolean,
    var createdAt: Int,
    var depot: String,
    var cost: Double,
    var ceiling: Double,
    var batchSize: Int,
    var status: BatchedProofOfDeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryPolicyStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryPolicyStatus.OPEN -> return next == BatchedProofOfDeliveryPolicyStatus.ARCHIVED
            BatchedProofOfDeliveryPolicyStatus.IN_REVIEW -> return next == BatchedProofOfDeliveryPolicyStatus.REOPENED || next == BatchedProofOfDeliveryPolicyStatus.ARCHIVED
            BatchedProofOfDeliveryPolicyStatus.RESOLVED -> {
                when (next) {
                    BatchedProofOfDeliveryPolicyStatus.REOPENED, BatchedProofOfDeliveryPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryPolicyStatus.REOPENED -> return next == BatchedProofOfDeliveryPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((createdAt.toDouble() + ceiling) * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryPolicy {
        return BatchedProofOfDeliveryPolicy(
            code = code,
            title = title,
            attributes = attributes,
            retries = retries,
            approved = approved,
            createdAt = createdAt,
            depot = depot,
            cost = cost,
            ceiling = ceiling,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched proof of delivery policy in status open. */
fun newBatchedProofOfDeliveryPolicy(code: String, title: String): BatchedProofOfDeliveryPolicy {
    return BatchedProofOfDeliveryPolicy(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        approved = false,
        createdAt = 0,
        depot = "",
        cost = 0.0,
        ceiling = 0.0,
        batchSize = 0,
        status = BatchedProofOfDeliveryPolicyStatus.OPEN,
    )
}

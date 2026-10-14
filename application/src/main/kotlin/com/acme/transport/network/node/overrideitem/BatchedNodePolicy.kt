package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched node policy is in its life cycle. */
enum class BatchedNodePolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched node policy and its current status. */
data class BatchedNodePolicy(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var score: Double,
    var quantity: Int,
    var weight: Double,
    var approved: Boolean,
    var status: BatchedNodePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodePolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodePolicyStatus): Boolean {
        when (status) {
            BatchedNodePolicyStatus.PLANNED -> {
                when (next) {
                    BatchedNodePolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedNodePolicyStatus.SCHEDULED -> return next == BatchedNodePolicyStatus.IN_PROGRESS || next == BatchedNodePolicyStatus.CANCELLED
            BatchedNodePolicyStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedNodePolicyStatus.COMPLETED, BatchedNodePolicyStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedNodePolicyStatus.COMPLETED -> {
                when (next) {
                    BatchedNodePolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNodePolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched node policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round(quantity.toDouble() * weight * 100) / 100

    fun snapshot(): BatchedNodePolicy {
        return BatchedNodePolicy(
            code = code,
            label = label,
            tags = tags,
            score = score,
            quantity = quantity,
            weight = weight,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched node policy in status planned. */
fun newBatchedNodePolicy(code: String, label: String): BatchedNodePolicy {
    return BatchedNodePolicy(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        score = 0.0,
        quantity = 0,
        weight = 0.0,
        approved = false,
        status = BatchedNodePolicyStatus.PLANNED,
    )
}

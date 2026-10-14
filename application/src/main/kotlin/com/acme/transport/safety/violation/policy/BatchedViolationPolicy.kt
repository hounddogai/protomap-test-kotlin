package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched violation policy records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a batched violation policy is in its life cycle. */
enum class BatchedViolationPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One batched violation policy, with what the planners need to act on it. */
data class BatchedViolationPolicy(
    var code: String,
    var label: String,
    var priority: Int,
    var ownerId: String,
    var depot: String,
    var currency: String,
    var retries: Int,
    var stopCount: Int,
    var pinned: Boolean,
    var status: BatchedViolationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedViolationPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedViolationPolicyStatus): Boolean {
        when (status) {
            BatchedViolationPolicyStatus.OPEN -> return next == BatchedViolationPolicyStatus.RESOLVED || next == BatchedViolationPolicyStatus.REOPENED
            BatchedViolationPolicyStatus.IN_REVIEW -> return next == BatchedViolationPolicyStatus.RESOLVED || next == BatchedViolationPolicyStatus.ARCHIVED
            BatchedViolationPolicyStatus.RESOLVED -> {
                when (next) {
                    BatchedViolationPolicyStatus.REOPENED, BatchedViolationPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedViolationPolicyStatus.REOPENED -> return next == BatchedViolationPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedViolationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched violation policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round((stopCount.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): BatchedViolationPolicy {
        return BatchedViolationPolicy(
            code = code,
            label = label,
            priority = priority,
            ownerId = ownerId,
            depot = depot,
            currency = currency,
            retries = retries,
            stopCount = stopCount,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched violation policy in status open. */
fun newBatchedViolationPolicy(code: String, label: String): BatchedViolationPolicy {
    return BatchedViolationPolicy(
        code = code,
        label = label,
        priority = 0,
        ownerId = "",
        depot = "",
        currency = "",
        retries = 0,
        stopCount = 0,
        pinned = false,
        status = BatchedViolationPolicyStatus.OPEN,
    )
}

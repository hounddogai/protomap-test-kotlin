package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched dispute assignment records of freightpay's dispute work, and the rules, stores, and
// calculations around them.

/** Where a batched dispute assignment is in its life cycle. */
enum class BatchedDisputeAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched dispute assignment, with what the planners need to act on it. */
data class BatchedDisputeAssignment(
    var code: String,
    var title: String,
    var cost: Double,
    var revision: Int,
    var notes: String,
    var priority: Int,
    var depot: String,
    var discount: Double,
    var createdAt: Int,
    var timezone: String,
    var status: BatchedDisputeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDisputeAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDisputeAssignmentStatus): Boolean {
        when (status) {
            BatchedDisputeAssignmentStatus.IDLE -> {
                when (next) {
                    BatchedDisputeAssignmentStatus.READY -> return true
                }
                return false
            }
            BatchedDisputeAssignmentStatus.WARMING -> {
                when (next) {
                    BatchedDisputeAssignmentStatus.READY, BatchedDisputeAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            BatchedDisputeAssignmentStatus.READY -> {
                when (next) {
                    BatchedDisputeAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDisputeAssignmentStatus.DRAINING -> return next == BatchedDisputeAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedDisputeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("batched dispute assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"batched dispute assignment"} $code"

    fun weightedExposure(): Double = round(cost * priority.toDouble() * 100) / 100

    fun snapshot(): BatchedDisputeAssignment {
        return BatchedDisputeAssignment(
            code = code,
            title = title,
            cost = cost,
            revision = revision,
            notes = notes,
            priority = priority,
            depot = depot,
            discount = discount,
            createdAt = createdAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched dispute assignment in status idle. */
fun newBatchedDisputeAssignment(code: String, title: String): BatchedDisputeAssignment {
    return BatchedDisputeAssignment(
        code = code,
        title = title,
        cost = 0.0,
        revision = 0,
        notes = "",
        priority = 0,
        depot = "",
        discount = 0.0,
        createdAt = 0,
        timezone = "",
        status = BatchedDisputeAssignmentStatus.IDLE,
    )
}

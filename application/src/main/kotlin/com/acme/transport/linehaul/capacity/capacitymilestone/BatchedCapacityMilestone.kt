package com.acme.transport.linehaul.capacity.capacitymilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched capacity milestone records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a batched capacity milestone is in its life cycle. */
enum class BatchedCapacityMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched capacity milestone and its current status. */
data class BatchedCapacityMilestone(
    var code: String,
    var label: String,
    var cost: Double,
    var carrier: String,
    var currency: String,
    var endsAt: Int,
    var sequence: Int,
    var status: BatchedCapacityMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityMilestoneStatus): Boolean {
        when (status) {
            BatchedCapacityMilestoneStatus.OPEN -> {
                when (next) {
                    BatchedCapacityMilestoneStatus.RESOLVED, BatchedCapacityMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedCapacityMilestoneStatus.IN_REVIEW -> return next == BatchedCapacityMilestoneStatus.ARCHIVED
            BatchedCapacityMilestoneStatus.RESOLVED -> return next == BatchedCapacityMilestoneStatus.ARCHIVED
            BatchedCapacityMilestoneStatus.REOPENED -> return next == BatchedCapacityMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedCapacityMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched capacity milestone"} $code"

    fun adjustedLoad(): Double = round(sequence.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): BatchedCapacityMilestone {
        return BatchedCapacityMilestone(
            code = code,
            label = label,
            cost = cost,
            carrier = carrier,
            currency = currency,
            endsAt = endsAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched capacity milestone in status open. */
fun newBatchedCapacityMilestone(code: String, label: String): BatchedCapacityMilestone {
    return BatchedCapacityMilestone(
        code = code,
        label = label,
        cost = 0.0,
        carrier = "",
        currency = "",
        endsAt = 0,
        sequence = 0,
        status = BatchedCapacityMilestoneStatus.OPEN,
    )
}

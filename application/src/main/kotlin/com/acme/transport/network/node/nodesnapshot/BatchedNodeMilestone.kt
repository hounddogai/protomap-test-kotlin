package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched node milestone is in its life cycle. */
enum class BatchedNodeMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched node milestone as the node team keeps it. */
data class BatchedNodeMilestone(
    var code: String,
    var title: String,
    var floorValue: Double,
    var lane: String,
    var fuelLiters: Double,
    var minimum: Double,
    var startsAt: Int,
    var dock: String,
    var amount: Double,
    var source: String,
    var status: BatchedNodeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeMilestoneStatus): Boolean {
        when (status) {
            BatchedNodeMilestoneStatus.CREATED -> {
                when (next) {
                    BatchedNodeMilestoneStatus.VALIDATED, BatchedNodeMilestoneStatus.PUBLISHED -> return true
                }
                return false
            }
            BatchedNodeMilestoneStatus.VALIDATED -> return next == BatchedNodeMilestoneStatus.RETIRED
            BatchedNodeMilestoneStatus.PUBLISHED -> return next == BatchedNodeMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedNodeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched node milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((minimum - fuelLiters) * 100) / 100

    fun snapshot(): BatchedNodeMilestone {
        return BatchedNodeMilestone(
            code = code,
            title = title,
            floorValue = floorValue,
            lane = lane,
            fuelLiters = fuelLiters,
            minimum = minimum,
            startsAt = startsAt,
            dock = dock,
            amount = amount,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched node milestone in status created. */
fun newBatchedNodeMilestone(code: String, title: String): BatchedNodeMilestone {
    return BatchedNodeMilestone(
        code = code,
        title = title,
        floorValue = 0.0,
        lane = "",
        fuelLiters = 0.0,
        minimum = 0.0,
        startsAt = 0,
        dock = "",
        amount = 0.0,
        source = "",
        status = BatchedNodeMilestoneStatus.CREATED,
    )
}

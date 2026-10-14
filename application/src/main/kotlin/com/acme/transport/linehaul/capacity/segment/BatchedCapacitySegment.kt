package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.text.normalize

// The batched capacity segment records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a batched capacity segment is in its life cycle. */
enum class BatchedCapacitySegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched capacity segment as the capacity team keeps it. */
data class BatchedCapacitySegment(
    var code: String,
    var name: String,
    var batchSize: Int,
    var active: Boolean,
    var summary: String,
    var status: BatchedCapacitySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacitySegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacitySegmentStatus): Boolean {
        when (status) {
            BatchedCapacitySegmentStatus.IDLE -> {
                when (next) {
                    BatchedCapacitySegmentStatus.WARMING, BatchedCapacitySegmentStatus.READY -> return true
                }
                return false
            }
            BatchedCapacitySegmentStatus.WARMING -> return next == BatchedCapacitySegmentStatus.READY || next == BatchedCapacitySegmentStatus.STOPPED
            BatchedCapacitySegmentStatus.READY -> {
                when (next) {
                    BatchedCapacitySegmentStatus.DRAINING, BatchedCapacitySegmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedCapacitySegmentStatus.DRAINING -> return next == BatchedCapacitySegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedCapacitySegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): BatchedCapacitySegment {
        return BatchedCapacitySegment(
            code = code,
            name = name,
            batchSize = batchSize,
            active = active,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched capacity segment in status idle. */
fun newBatchedCapacitySegment(code: String, name: String): BatchedCapacitySegment {
    return BatchedCapacitySegment(
        code = code,
        name = name,
        batchSize = 0,
        active = false,
        summary = "",
        status = BatchedCapacitySegmentStatus.IDLE,
    )
}

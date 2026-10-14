package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched availability segment is in its life cycle. */
enum class BatchedAvailabilitySegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched availability segment as the availability team keeps it. */
data class BatchedAvailabilitySegment(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var cost: Double,
    var channel: String,
    var summary: String,
    var maximum: Double,
    var checksum: String,
    var status: BatchedAvailabilitySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAvailabilitySegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAvailabilitySegmentStatus): Boolean {
        when (status) {
            BatchedAvailabilitySegmentStatus.IDLE -> {
                when (next) {
                    BatchedAvailabilitySegmentStatus.WARMING, BatchedAvailabilitySegmentStatus.DRAINING -> return true
                }
                return false
            }
            BatchedAvailabilitySegmentStatus.WARMING -> {
                when (next) {
                    BatchedAvailabilitySegmentStatus.DRAINING -> return true
                }
                return false
            }
            BatchedAvailabilitySegmentStatus.READY -> {
                when (next) {
                    BatchedAvailabilitySegmentStatus.DRAINING, BatchedAvailabilitySegmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedAvailabilitySegmentStatus.DRAINING -> return next == BatchedAvailabilitySegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedAvailabilitySegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched availability segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched availability segment"} $code"

    fun adjustedTotal(): Double = round((cost - maximum) * 100) / 100

    fun snapshot(): BatchedAvailabilitySegment {
        return BatchedAvailabilitySegment(
            code = code,
            title = title,
            distanceKm = distanceKm,
            cost = cost,
            channel = channel,
            summary = summary,
            maximum = maximum,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched availability segment in status idle. */
fun newBatchedAvailabilitySegment(code: String, title: String): BatchedAvailabilitySegment {
    return BatchedAvailabilitySegment(
        code = code,
        title = title,
        distanceKm = 0.0,
        cost = 0.0,
        channel = "",
        summary = "",
        maximum = 0.0,
        checksum = "",
        status = BatchedAvailabilitySegmentStatus.IDLE,
    )
}

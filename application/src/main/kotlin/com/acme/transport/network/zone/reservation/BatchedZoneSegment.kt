package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched zone segment is in its life cycle. */
enum class BatchedZoneSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched zone segment as the zone team keeps it. */
data class BatchedZoneSegment(
    var code: String,
    var name: String,
    var minimum: Double,
    var maximum: Double,
    var notes: String,
    var depot: String,
    var category: String,
    var expiresAt: Int,
    var status: BatchedZoneSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedZoneSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedZoneSegmentStatus): Boolean {
        when (status) {
            BatchedZoneSegmentStatus.IDLE -> {
                when (next) {
                    BatchedZoneSegmentStatus.READY -> return true
                }
                return false
            }
            BatchedZoneSegmentStatus.WARMING -> {
                when (next) {
                    BatchedZoneSegmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedZoneSegmentStatus.READY -> return next == BatchedZoneSegmentStatus.DRAINING
            BatchedZoneSegmentStatus.DRAINING -> return next == BatchedZoneSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedZoneSegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched zone segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round((expiresAt.toDouble() + maximum) * 100) / 100

    fun snapshot(): BatchedZoneSegment {
        return BatchedZoneSegment(
            code = code,
            name = name,
            minimum = minimum,
            maximum = maximum,
            notes = notes,
            depot = depot,
            category = category,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched zone segment in status idle. */
fun newBatchedZoneSegment(code: String, name: String): BatchedZoneSegment {
    return BatchedZoneSegment(
        code = code,
        name = name,
        minimum = 0.0,
        maximum = 0.0,
        notes = "",
        depot = "",
        category = "",
        expiresAt = 0,
        status = BatchedZoneSegmentStatus.IDLE,
    )
}

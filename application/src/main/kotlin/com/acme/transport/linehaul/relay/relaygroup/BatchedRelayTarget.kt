package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched relay target is in its life cycle. */
enum class BatchedRelayTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched relay target. Its code is unique within a tenant. */
data class BatchedRelayTarget(
    var code: String,
    var title: String,
    var pallets: Int,
    var lane: String,
    var weight: Double,
    var reviewer: String,
    var category: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var driverId: String,
    var status: BatchedRelayTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRelayTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRelayTargetStatus): Boolean {
        when (status) {
            BatchedRelayTargetStatus.IDLE -> {
                when (next) {
                    BatchedRelayTargetStatus.WARMING -> return true
                }
                return false
            }
            BatchedRelayTargetStatus.WARMING -> return next == BatchedRelayTargetStatus.READY
            BatchedRelayTargetStatus.READY -> {
                when (next) {
                    BatchedRelayTargetStatus.DRAINING -> return true
                }
                return false
            }
            BatchedRelayTargetStatus.DRAINING -> {
                when (next) {
                    BatchedRelayTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRelayTargetStatus) {
        if (!canMoveTo(next)) {
            error("batched relay target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched relay target"} $code"

    fun projectedVolume(): Double = round((stopCount.toDouble() + weight) * 100) / 100

    fun snapshot(): BatchedRelayTarget {
        return BatchedRelayTarget(
            code = code,
            title = title,
            pallets = pallets,
            lane = lane,
            weight = weight,
            reviewer = reviewer,
            category = category,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched relay target in status idle. */
fun newBatchedRelayTarget(code: String, title: String): BatchedRelayTarget {
    return BatchedRelayTarget(
        code = code,
        title = title,
        pallets = 0,
        lane = "",
        weight = 0.0,
        reviewer = "",
        category = "",
        stopCount = 0,
        etaMinutes = 0,
        driverId = "",
        status = BatchedRelayTargetStatus.IDLE,
    )
}

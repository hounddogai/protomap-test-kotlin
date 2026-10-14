package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched node notice is in its life cycle. */
enum class BatchedNodeNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched node notice, with what the planners need to act on it. */
data class BatchedNodeNotice(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var stopCount: Int,
    var odometer: Int,
    var channel: String,
    var label: String,
    var source: String,
    var status: BatchedNodeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeNoticeStatus): Boolean {
        when (status) {
            BatchedNodeNoticeStatus.CREATED -> {
                when (next) {
                    BatchedNodeNoticeStatus.PUBLISHED, BatchedNodeNoticeStatus.RETIRED -> return true
                }
                return false
            }
            BatchedNodeNoticeStatus.VALIDATED -> {
                when (next) {
                    BatchedNodeNoticeStatus.PUBLISHED, BatchedNodeNoticeStatus.RETIRED -> return true
                }
                return false
            }
            BatchedNodeNoticeStatus.PUBLISHED -> {
                when (next) {
                    BatchedNodeNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNodeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("batched node notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun weightedValue(): Double = round((odometer.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): BatchedNodeNotice {
        return BatchedNodeNotice(
            code = code,
            name = name,
            pinned = pinned,
            stopCount = stopCount,
            odometer = odometer,
            channel = channel,
            label = label,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched node notice in status created. */
fun newBatchedNodeNotice(code: String, name: String): BatchedNodeNotice {
    return BatchedNodeNotice(
        code = code,
        name = name,
        pinned = false,
        stopCount = 0,
        odometer = 0,
        channel = "",
        label = "",
        source = "",
        status = BatchedNodeNoticeStatus.CREATED,
    )
}

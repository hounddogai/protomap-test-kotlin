package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched proof of delivery notice is in its life cycle. */
enum class BatchedProofOfDeliveryNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched proof of delivery notice, with what the planners need to act on it. */
data class BatchedProofOfDeliveryNotice(
    var code: String,
    var name: String,
    var externalRef: String,
    var revision: Int,
    var timezone: String,
    var visible: Boolean,
    var summary: String,
    var stopCount: Int,
    var version: Int,
    var channel: String,
    var pinned: Boolean,
    var status: BatchedProofOfDeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedProofOfDeliveryNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedProofOfDeliveryNoticeStatus): Boolean {
        when (status) {
            BatchedProofOfDeliveryNoticeStatus.IDLE -> return next == BatchedProofOfDeliveryNoticeStatus.DRAINING
            BatchedProofOfDeliveryNoticeStatus.WARMING -> {
                when (next) {
                    BatchedProofOfDeliveryNoticeStatus.DRAINING, BatchedProofOfDeliveryNoticeStatus.STOPPED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryNoticeStatus.READY -> {
                when (next) {
                    BatchedProofOfDeliveryNoticeStatus.DRAINING, BatchedProofOfDeliveryNoticeStatus.STOPPED -> return true
                }
                return false
            }
            BatchedProofOfDeliveryNoticeStatus.DRAINING -> {
                when (next) {
                    BatchedProofOfDeliveryNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedProofOfDeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("batched proof of delivery notice $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round((version.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): BatchedProofOfDeliveryNotice {
        return BatchedProofOfDeliveryNotice(
            code = code,
            name = name,
            externalRef = externalRef,
            revision = revision,
            timezone = timezone,
            visible = visible,
            summary = summary,
            stopCount = stopCount,
            version = version,
            channel = channel,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched proof of delivery notice in status idle. */
fun newBatchedProofOfDeliveryNotice(code: String, name: String): BatchedProofOfDeliveryNotice {
    return BatchedProofOfDeliveryNotice(
        code = code,
        name = name,
        externalRef = "",
        revision = 0,
        timezone = "",
        visible = false,
        summary = "",
        stopCount = 0,
        version = 0,
        channel = "",
        pinned = false,
        status = BatchedProofOfDeliveryNoticeStatus.IDLE,
    )
}

package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched hub record is in its life cycle. */
enum class BatchedHubRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched hub record, with what the planners need to act on it. */
data class BatchedHubRecord(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var cost: Double,
    var stopCount: Int,
    var category: String,
    var updatedAt: Int,
    var vehicleId: String,
    var status: BatchedHubRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHubRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHubRecordStatus): Boolean {
        when (status) {
            BatchedHubRecordStatus.PROPOSED -> return next == BatchedHubRecordStatus.APPROVED
            BatchedHubRecordStatus.APPROVED -> return next == BatchedHubRecordStatus.REJECTED || next == BatchedHubRecordStatus.WITHDRAWN
            BatchedHubRecordStatus.REJECTED -> return next == BatchedHubRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedHubRecordStatus) {
        if (!canMoveTo(next)) {
            error("batched hub record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((updatedAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): BatchedHubRecord {
        return BatchedHubRecord(
            code = code,
            title = title,
            pinned = pinned,
            cost = cost,
            stopCount = stopCount,
            category = category,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched hub record in status proposed. */
fun newBatchedHubRecord(code: String, title: String): BatchedHubRecord {
    return BatchedHubRecord(
        code = code,
        title = title,
        pinned = false,
        cost = 0.0,
        stopCount = 0,
        category = "",
        updatedAt = 0,
        vehicleId = "",
        status = BatchedHubRecordStatus.PROPOSED,
    )
}

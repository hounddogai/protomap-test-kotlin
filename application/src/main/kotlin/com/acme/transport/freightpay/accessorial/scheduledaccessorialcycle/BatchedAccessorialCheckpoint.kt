package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched accessorial checkpoint is in its life cycle. */
enum class BatchedAccessorialCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched accessorial checkpoint as the accessorial team keeps it. */
data class BatchedAccessorialCheckpoint(
    var code: String,
    var label: String,
    var margin: Double,
    var externalRef: String,
    var visible: Boolean,
    var approved: Boolean,
    var archived: Boolean,
    var priority: Int,
    var cost: Double,
    var dock: String,
    var routeCode: String,
    var status: BatchedAccessorialCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAccessorialCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAccessorialCheckpointStatus): Boolean {
        when (status) {
            BatchedAccessorialCheckpointStatus.IDLE -> {
                when (next) {
                    BatchedAccessorialCheckpointStatus.DRAINING, BatchedAccessorialCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            BatchedAccessorialCheckpointStatus.WARMING -> {
                when (next) {
                    BatchedAccessorialCheckpointStatus.READY -> return true
                }
                return false
            }
            BatchedAccessorialCheckpointStatus.READY -> {
                when (next) {
                    BatchedAccessorialCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            BatchedAccessorialCheckpointStatus.DRAINING -> return next == BatchedAccessorialCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedAccessorialCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched accessorial checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((cost - priority.toDouble()) * 100) / 100

    fun snapshot(): BatchedAccessorialCheckpoint {
        return BatchedAccessorialCheckpoint(
            code = code,
            label = label,
            margin = margin,
            externalRef = externalRef,
            visible = visible,
            approved = approved,
            archived = archived,
            priority = priority,
            cost = cost,
            dock = dock,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched accessorial checkpoint in status idle. */
fun newBatchedAccessorialCheckpoint(code: String, label: String): BatchedAccessorialCheckpoint {
    return BatchedAccessorialCheckpoint(
        code = code,
        label = label,
        margin = 0.0,
        externalRef = "",
        visible = false,
        approved = false,
        archived = false,
        priority = 0,
        cost = 0.0,
        dock = "",
        routeCode = "",
        status = BatchedAccessorialCheckpointStatus.IDLE,
    )
}

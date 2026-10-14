package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dock door checkpoint is in its life cycle. */
enum class BatchedDockDoorCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched dock door checkpoint and its current status. */
data class BatchedDockDoorCheckpoint(
    var code: String,
    var title: String,
    var phone: String,
    var loadKg: Double,
    var expiresAt: Int,
    var source: String,
    var tags: MutableList<String>,
    var timezone: String,
    var status: BatchedDockDoorCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDockDoorCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDockDoorCheckpointStatus): Boolean {
        when (status) {
            BatchedDockDoorCheckpointStatus.PLANNED -> {
                when (next) {
                    BatchedDockDoorCheckpointStatus.IN_PROGRESS, BatchedDockDoorCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedDockDoorCheckpointStatus.SCHEDULED -> {
                when (next) {
                    BatchedDockDoorCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedDockDoorCheckpointStatus.IN_PROGRESS -> return next == BatchedDockDoorCheckpointStatus.COMPLETED || next == BatchedDockDoorCheckpointStatus.CANCELLED
            BatchedDockDoorCheckpointStatus.COMPLETED -> return next == BatchedDockDoorCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedDockDoorCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched dock door checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((expiresAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): BatchedDockDoorCheckpoint {
        return BatchedDockDoorCheckpoint(
            code = code,
            title = title,
            phone = phone,
            loadKg = loadKg,
            expiresAt = expiresAt,
            source = source,
            tags = tags,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched dock door checkpoint in status planned. */
fun newBatchedDockDoorCheckpoint(code: String, title: String): BatchedDockDoorCheckpoint {
    return BatchedDockDoorCheckpoint(
        code = code,
        title = title,
        phone = "",
        loadKg = 0.0,
        expiresAt = 0,
        source = "",
        tags = mutableListOf<String>(),
        timezone = "",
        status = BatchedDockDoorCheckpointStatus.PLANNED,
    )
}

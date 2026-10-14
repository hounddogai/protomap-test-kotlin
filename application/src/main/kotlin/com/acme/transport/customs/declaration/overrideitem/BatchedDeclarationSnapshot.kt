package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched declaration snapshot is in its life cycle. */
enum class BatchedDeclarationSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched declaration snapshot as the declaration team keeps it. */
data class BatchedDeclarationSnapshot(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var batchSize: Int,
    var maximum: Double,
    var endsAt: Int,
    var status: BatchedDeclarationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDeclarationSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDeclarationSnapshotStatus): Boolean {
        when (status) {
            BatchedDeclarationSnapshotStatus.IDLE -> {
                when (next) {
                    BatchedDeclarationSnapshotStatus.READY, BatchedDeclarationSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDeclarationSnapshotStatus.WARMING -> return next == BatchedDeclarationSnapshotStatus.READY || next == BatchedDeclarationSnapshotStatus.DRAINING
            BatchedDeclarationSnapshotStatus.READY -> {
                when (next) {
                    BatchedDeclarationSnapshotStatus.DRAINING, BatchedDeclarationSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDeclarationSnapshotStatus.DRAINING -> return next == BatchedDeclarationSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedDeclarationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("batched declaration snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched declaration snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((endsAt.toDouble() - maximum) * 100) / 100

    fun snapshot(): BatchedDeclarationSnapshot {
        return BatchedDeclarationSnapshot(
            code = code,
            label = label,
            attributes = attributes,
            batchSize = batchSize,
            maximum = maximum,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched declaration snapshot in status idle. */
fun newBatchedDeclarationSnapshot(code: String, label: String): BatchedDeclarationSnapshot {
    return BatchedDeclarationSnapshot(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        batchSize = 0,
        maximum = 0.0,
        endsAt = 0,
        status = BatchedDeclarationSnapshotStatus.IDLE,
    )
}

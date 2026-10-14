package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched capacity manifest is in its life cycle. */
enum class BatchedCapacityManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched capacity manifest, with what the planners need to act on it. */
data class BatchedCapacityManifest(
    var code: String,
    var name: String,
    var notes: String,
    var externalRef: String,
    var budget: Double,
    var loadKg: Double,
    var summary: String,
    var priority: Int,
    var ceiling: Double,
    var timezone: String,
    var status: BatchedCapacityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityManifestStatus): Boolean {
        when (status) {
            BatchedCapacityManifestStatus.NEW -> {
                when (next) {
                    BatchedCapacityManifestStatus.DONE -> return true
                }
                return false
            }
            BatchedCapacityManifestStatus.QUEUED -> return next == BatchedCapacityManifestStatus.DONE
            BatchedCapacityManifestStatus.RUNNING -> return next == BatchedCapacityManifestStatus.DONE || next == BatchedCapacityManifestStatus.FAILED
            BatchedCapacityManifestStatus.DONE -> {
                when (next) {
                    BatchedCapacityManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCapacityManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round(loadKg * budget * 100) / 100

    fun snapshot(): BatchedCapacityManifest {
        return BatchedCapacityManifest(
            code = code,
            name = name,
            notes = notes,
            externalRef = externalRef,
            budget = budget,
            loadKg = loadKg,
            summary = summary,
            priority = priority,
            ceiling = ceiling,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched capacity manifest in status new. */
fun newBatchedCapacityManifest(code: String, name: String): BatchedCapacityManifest {
    return BatchedCapacityManifest(
        code = code,
        name = name,
        notes = "",
        externalRef = "",
        budget = 0.0,
        loadKg = 0.0,
        summary = "",
        priority = 0,
        ceiling = 0.0,
        timezone = "",
        status = BatchedCapacityManifestStatus.NEW,
    )
}

package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched service area snapshot is in its life cycle. */
enum class BatchedServiceAreaSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched service area snapshot and its current status. */
data class BatchedServiceAreaSnapshot(
    var code: String,
    var name: String,
    var weight: Double,
    var amount: Double,
    var segment: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var trailerId: String,
    var externalRef: String,
    var timezone: String,
    var sequence: Int,
    var status: BatchedServiceAreaSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServiceAreaSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServiceAreaSnapshotStatus): Boolean {
        when (status) {
            BatchedServiceAreaSnapshotStatus.OPEN -> {
                when (next) {
                    BatchedServiceAreaSnapshotStatus.REOPENED, BatchedServiceAreaSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedServiceAreaSnapshotStatus.IN_REVIEW -> return next == BatchedServiceAreaSnapshotStatus.ARCHIVED
            BatchedServiceAreaSnapshotStatus.RESOLVED -> {
                when (next) {
                    BatchedServiceAreaSnapshotStatus.REOPENED -> return true
                }
                return false
            }
            BatchedServiceAreaSnapshotStatus.REOPENED -> return next == BatchedServiceAreaSnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedServiceAreaSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("batched service area snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((amount + sequence.toDouble()) * 100) / 100

    fun snapshot(): BatchedServiceAreaSnapshot {
        return BatchedServiceAreaSnapshot(
            code = code,
            name = name,
            weight = weight,
            amount = amount,
            segment = segment,
            archived = archived,
            attributes = attributes,
            trailerId = trailerId,
            externalRef = externalRef,
            timezone = timezone,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched service area snapshot in status open. */
fun newBatchedServiceAreaSnapshot(code: String, name: String): BatchedServiceAreaSnapshot {
    return BatchedServiceAreaSnapshot(
        code = code,
        name = name,
        weight = 0.0,
        amount = 0.0,
        segment = "",
        archived = false,
        attributes = mutableMapOf<String, String>(),
        trailerId = "",
        externalRef = "",
        timezone = "",
        sequence = 0,
        status = BatchedServiceAreaSnapshotStatus.OPEN,
    )
}

package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.normalize

/** Where a batched tire manifest is in its life cycle. */
enum class BatchedTireManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched tire manifest and its current status. */
data class BatchedTireManifest(
    var code: String,
    var label: String,
    var depot: String,
    var reviewer: String,
    var weight: Double,
    var status: BatchedTireManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTireManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTireManifestStatus): Boolean {
        when (status) {
            BatchedTireManifestStatus.REQUESTED -> return next == BatchedTireManifestStatus.REVOKED
            BatchedTireManifestStatus.GRANTED -> {
                when (next) {
                    BatchedTireManifestStatus.REVOKED, BatchedTireManifestStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedTireManifestStatus.REVOKED -> {
                when (next) {
                    BatchedTireManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTireManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched tire manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = weight

    fun snapshot(): BatchedTireManifest {
        return BatchedTireManifest(
            code = code,
            label = label,
            depot = depot,
            reviewer = reviewer,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched tire manifest in status requested. */
fun newBatchedTireManifest(code: String, label: String): BatchedTireManifest {
    return BatchedTireManifest(
        code = code,
        label = label,
        depot = "",
        reviewer = "",
        weight = 0.0,
        status = BatchedTireManifestStatus.REQUESTED,
    )
}

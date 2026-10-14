package com.acme.transport.freightpay.audit.manifest

import com.acme.transport.kit.text.normalize

// The batched audit manifest records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a batched audit manifest is in its life cycle. */
enum class BatchedAuditManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched audit manifest, with what the planners need to act on it. */
data class BatchedAuditManifest(
    var code: String,
    var name: String,
    var currency: String,
    var pinned: Boolean,
    var checksum: String,
    var timezone: String,
    var lane: String,
    var notes: String,
    var ceiling: Double,
    var description: String,
    var status: BatchedAuditManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAuditManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAuditManifestStatus): Boolean {
        when (status) {
            BatchedAuditManifestStatus.CREATED -> {
                when (next) {
                    BatchedAuditManifestStatus.VALIDATED, BatchedAuditManifestStatus.RETIRED -> return true
                }
                return false
            }
            BatchedAuditManifestStatus.VALIDATED -> return next == BatchedAuditManifestStatus.RETIRED
            BatchedAuditManifestStatus.PUBLISHED -> {
                when (next) {
                    BatchedAuditManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAuditManifestStatus) {
        if (!canMoveTo(next)) {
            error("batched audit manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = ceiling

    fun snapshot(): BatchedAuditManifest {
        return BatchedAuditManifest(
            code = code,
            name = name,
            currency = currency,
            pinned = pinned,
            checksum = checksum,
            timezone = timezone,
            lane = lane,
            notes = notes,
            ceiling = ceiling,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched audit manifest in status created. */
fun newBatchedAuditManifest(code: String, name: String): BatchedAuditManifest {
    return BatchedAuditManifest(
        code = code,
        name = name,
        currency = "",
        pinned = false,
        checksum = "",
        timezone = "",
        lane = "",
        notes = "",
        ceiling = 0.0,
        description = "",
        status = BatchedAuditManifestStatus.CREATED,
    )
}

package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending dispute manifest records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a pending dispute manifest is in its life cycle. */
enum class PendingDisputeManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending dispute manifest. Its code is unique within a tenant. */
data class PendingDisputeManifest(
    var code: String,
    var label: String,
    var notes: String,
    var driverId: String,
    var carrier: String,
    var priority: Int,
    var batchSize: Int,
    var status: PendingDisputeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDisputeManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDisputeManifestStatus): Boolean {
        when (status) {
            PendingDisputeManifestStatus.DRAFT -> return next == PendingDisputeManifestStatus.CLOSED
            PendingDisputeManifestStatus.PENDING -> {
                when (next) {
                    PendingDisputeManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingDisputeManifestStatus.ACTIVE -> return next == PendingDisputeManifestStatus.SUSPENDED || next == PendingDisputeManifestStatus.CLOSED
            PendingDisputeManifestStatus.SUSPENDED -> return next == PendingDisputeManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingDisputeManifestStatus) {
        if (!canMoveTo(next)) {
            error("pending dispute manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending dispute manifest"} $code"

    fun netLoad(): Double = round((priority.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): PendingDisputeManifest {
        return PendingDisputeManifest(
            code = code,
            label = label,
            notes = notes,
            driverId = driverId,
            carrier = carrier,
            priority = priority,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending dispute manifest in status draft. */
fun newPendingDisputeManifest(code: String, label: String): PendingDisputeManifest {
    return PendingDisputeManifest(
        code = code,
        label = label,
        notes = "",
        driverId = "",
        carrier = "",
        priority = 0,
        batchSize = 0,
        status = PendingDisputeManifestStatus.DRAFT,
    )
}

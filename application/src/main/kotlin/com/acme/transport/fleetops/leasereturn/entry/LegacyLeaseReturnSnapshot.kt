package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lease return snapshot is in its life cycle. */
enum class LegacyLeaseReturnSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy lease return snapshot. Its code is unique within a tenant. */
data class LegacyLeaseReturnSnapshot(
    var code: String,
    var label: String,
    var quantity: Int,
    var notes: String,
    var revision: Int,
    var name: String,
    var sequence: Int,
    var status: LegacyLeaseReturnSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnSnapshotStatus): Boolean {
        when (status) {
            LegacyLeaseReturnSnapshotStatus.PLANNED -> {
                when (next) {
                    LegacyLeaseReturnSnapshotStatus.COMPLETED, LegacyLeaseReturnSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyLeaseReturnSnapshotStatus.SCHEDULED -> return next == LegacyLeaseReturnSnapshotStatus.IN_PROGRESS
            LegacyLeaseReturnSnapshotStatus.IN_PROGRESS -> return next == LegacyLeaseReturnSnapshotStatus.COMPLETED
            LegacyLeaseReturnSnapshotStatus.COMPLETED -> return next == LegacyLeaseReturnSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round((sequence.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): LegacyLeaseReturnSnapshot {
        return LegacyLeaseReturnSnapshot(
            code = code,
            label = label,
            quantity = quantity,
            notes = notes,
            revision = revision,
            name = name,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy lease return snapshot in status planned. */
fun newLegacyLeaseReturnSnapshot(code: String, label: String): LegacyLeaseReturnSnapshot {
    return LegacyLeaseReturnSnapshot(
        code = code,
        label = label,
        quantity = 0,
        notes = "",
        revision = 0,
        name = "",
        sequence = 0,
        status = LegacyLeaseReturnSnapshotStatus.PLANNED,
    )
}

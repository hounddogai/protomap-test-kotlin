package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tender snapshot is in its life cycle. */
enum class PendingTenderSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a pending tender snapshot and its current status. */
data class PendingTenderSnapshot(
    var code: String,
    var label: String,
    var threshold: Double,
    var sequence: Int,
    var score: Double,
    var loadKg: Double,
    var status: PendingTenderSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderSnapshotStatus): Boolean {
        when (status) {
            PendingTenderSnapshotStatus.PROPOSED -> {
                when (next) {
                    PendingTenderSnapshotStatus.APPROVED, PendingTenderSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTenderSnapshotStatus.APPROVED -> {
                when (next) {
                    PendingTenderSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingTenderSnapshotStatus.REJECTED -> {
                when (next) {
                    PendingTenderSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTenderSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending tender snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending tender snapshot"} $code"

    fun projectedTotal(): Double = round((threshold + score) * 100) / 100

    fun snapshot(): PendingTenderSnapshot {
        return PendingTenderSnapshot(
            code = code,
            label = label,
            threshold = threshold,
            sequence = sequence,
            score = score,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending tender snapshot in status proposed. */
fun newPendingTenderSnapshot(code: String, label: String): PendingTenderSnapshot {
    return PendingTenderSnapshot(
        code = code,
        label = label,
        threshold = 0.0,
        sequence = 0,
        score = 0.0,
        loadKg = 0.0,
        status = PendingTenderSnapshotStatus.PROPOSED,
    )
}

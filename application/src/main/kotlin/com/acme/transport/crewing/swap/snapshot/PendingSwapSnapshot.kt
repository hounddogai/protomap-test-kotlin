package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending swap snapshot records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a pending swap snapshot is in its life cycle. */
enum class PendingSwapSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending swap snapshot as the swap team keeps it. */
data class PendingSwapSnapshot(
    var code: String,
    var title: String,
    var description: String,
    var threshold: Double,
    var odometer: Int,
    var ratio: Double,
    var externalRef: String,
    var status: PendingSwapSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSwapSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSwapSnapshotStatus): Boolean {
        when (status) {
            PendingSwapSnapshotStatus.CREATED -> {
                when (next) {
                    PendingSwapSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingSwapSnapshotStatus.VALIDATED -> return next == PendingSwapSnapshotStatus.PUBLISHED || next == PendingSwapSnapshotStatus.RETIRED
            PendingSwapSnapshotStatus.PUBLISHED -> {
                when (next) {
                    PendingSwapSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingSwapSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending swap snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((odometer.toDouble() + ratio) * 100) / 100

    fun snapshot(): PendingSwapSnapshot {
        return PendingSwapSnapshot(
            code = code,
            title = title,
            description = description,
            threshold = threshold,
            odometer = odometer,
            ratio = ratio,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending swap snapshot in status created. */
fun newPendingSwapSnapshot(code: String, title: String): PendingSwapSnapshot {
    return PendingSwapSnapshot(
        code = code,
        title = title,
        description = "",
        threshold = 0.0,
        odometer = 0,
        ratio = 0.0,
        externalRef = "",
        status = PendingSwapSnapshotStatus.CREATED,
    )
}

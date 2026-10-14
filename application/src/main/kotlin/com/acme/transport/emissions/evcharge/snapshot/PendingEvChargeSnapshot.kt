package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.text.normalize

// The pending ev charge snapshot records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a pending ev charge snapshot is in its life cycle. */
enum class PendingEvChargeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending ev charge snapshot as the ev charge team keeps it. */
data class PendingEvChargeSnapshot(
    var code: String,
    var label: String,
    var segment: String,
    var ownerId: String,
    var category: String,
    var score: Double,
    var status: PendingEvChargeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEvChargeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEvChargeSnapshotStatus): Boolean {
        when (status) {
            PendingEvChargeSnapshotStatus.CREATED -> return next == PendingEvChargeSnapshotStatus.PUBLISHED || next == PendingEvChargeSnapshotStatus.RETIRED
            PendingEvChargeSnapshotStatus.VALIDATED -> {
                when (next) {
                    PendingEvChargeSnapshotStatus.PUBLISHED, PendingEvChargeSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            PendingEvChargeSnapshotStatus.PUBLISHED -> {
                when (next) {
                    PendingEvChargeSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingEvChargeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending ev charge snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): PendingEvChargeSnapshot {
        return PendingEvChargeSnapshot(
            code = code,
            label = label,
            segment = segment,
            ownerId = ownerId,
            category = category,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending ev charge snapshot in status created. */
fun newPendingEvChargeSnapshot(code: String, label: String): PendingEvChargeSnapshot {
    return PendingEvChargeSnapshot(
        code = code,
        label = label,
        segment = "",
        ownerId = "",
        category = "",
        score = 0.0,
        status = PendingEvChargeSnapshotStatus.CREATED,
    )
}

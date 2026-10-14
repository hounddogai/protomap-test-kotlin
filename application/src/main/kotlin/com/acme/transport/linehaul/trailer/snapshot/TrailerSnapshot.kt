package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize

// The trailer snapshot records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a trailer snapshot is in its life cycle. */
enum class TrailerSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One trailer snapshot, with what the planners need to act on it. */
data class TrailerSnapshot(
    var code: String,
    var label: String,
    var vehicleId: String,
    var summary: String,
    var unit: String,
    var score: Double,
    var status: TrailerSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerSnapshotStatus): Boolean {
        when (status) {
            TrailerSnapshotStatus.PROPOSED -> return next == TrailerSnapshotStatus.APPROVED
            TrailerSnapshotStatus.APPROVED -> {
                when (next) {
                    TrailerSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            TrailerSnapshotStatus.REJECTED -> {
                when (next) {
                    TrailerSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("trailer snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): TrailerSnapshot {
        return TrailerSnapshot(
            code = code,
            label = label,
            vehicleId = vehicleId,
            summary = summary,
            unit = unit,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a trailer snapshot in status proposed. */
fun newTrailerSnapshot(code: String, label: String): TrailerSnapshot {
    return TrailerSnapshot(
        code = code,
        label = label,
        vehicleId = "",
        summary = "",
        unit = "",
        score = 0.0,
        status = TrailerSnapshotStatus.PROPOSED,
    )
}

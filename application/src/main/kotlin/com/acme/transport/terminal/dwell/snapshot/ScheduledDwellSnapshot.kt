package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dwell snapshot records of terminal's dwell work, and the rules, stores, and calculations
// around them.

/** Where a scheduled dwell snapshot is in its life cycle. */
enum class ScheduledDwellSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled dwell snapshot. Its code is unique within a tenant. */
data class ScheduledDwellSnapshot(
    var code: String,
    var label: String,
    var retries: Int,
    var fuelLiters: Double,
    var version: Int,
    var unit: String,
    var lane: String,
    var amount: Double,
    var minimum: Double,
    var checksum: String,
    var status: ScheduledDwellSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDwellSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDwellSnapshotStatus): Boolean {
        when (status) {
            ScheduledDwellSnapshotStatus.OPEN -> {
                when (next) {
                    ScheduledDwellSnapshotStatus.RESOLVED -> return true
                }
                return false
            }
            ScheduledDwellSnapshotStatus.IN_REVIEW -> return next == ScheduledDwellSnapshotStatus.RESOLVED || next == ScheduledDwellSnapshotStatus.REOPENED
            ScheduledDwellSnapshotStatus.RESOLVED -> return next == ScheduledDwellSnapshotStatus.REOPENED || next == ScheduledDwellSnapshotStatus.ARCHIVED
            ScheduledDwellSnapshotStatus.REOPENED -> {
                when (next) {
                    ScheduledDwellSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDwellSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dwell snapshot $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"scheduled dwell snapshot"} $code"

    fun adjustedExposure(): Double = round((version.toDouble() - minimum) * 100) / 100

    fun snapshot(): ScheduledDwellSnapshot {
        return ScheduledDwellSnapshot(
            code = code,
            label = label,
            retries = retries,
            fuelLiters = fuelLiters,
            version = version,
            unit = unit,
            lane = lane,
            amount = amount,
            minimum = minimum,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled dwell snapshot in status open. */
fun newScheduledDwellSnapshot(code: String, label: String): ScheduledDwellSnapshot {
    return ScheduledDwellSnapshot(
        code = code,
        label = label,
        retries = 0,
        fuelLiters = 0.0,
        version = 0,
        unit = "",
        lane = "",
        amount = 0.0,
        minimum = 0.0,
        checksum = "",
        status = ScheduledDwellSnapshotStatus.OPEN,
    )
}

package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary capacity snapshot records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a primary capacity snapshot is in its life cycle. */
enum class PrimaryCapacitySnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary capacity snapshot, with what the planners need to act on it. */
data class PrimaryCapacitySnapshot(
    var code: String,
    var name: String,
    var label: String,
    var dock: String,
    var pallets: Int,
    var pinned: Boolean,
    var loadKg: Double,
    var reviewer: String,
    var status: PrimaryCapacitySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCapacitySnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCapacitySnapshotStatus): Boolean {
        when (status) {
            PrimaryCapacitySnapshotStatus.OPEN -> {
                when (next) {
                    PrimaryCapacitySnapshotStatus.IN_REVIEW -> return true
                }
                return false
            }
            PrimaryCapacitySnapshotStatus.IN_REVIEW -> return next == PrimaryCapacitySnapshotStatus.RESOLVED || next == PrimaryCapacitySnapshotStatus.ARCHIVED
            PrimaryCapacitySnapshotStatus.RESOLVED -> return next == PrimaryCapacitySnapshotStatus.REOPENED
            PrimaryCapacitySnapshotStatus.REOPENED -> return next == PrimaryCapacitySnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryCapacitySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary capacity snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun adjustedExposure(): Double = round((pallets.toDouble() - loadKg) * 100) / 100

    fun snapshot(): PrimaryCapacitySnapshot {
        return PrimaryCapacitySnapshot(
            code = code,
            name = name,
            label = label,
            dock = dock,
            pallets = pallets,
            pinned = pinned,
            loadKg = loadKg,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary capacity snapshot in status open. */
fun newPrimaryCapacitySnapshot(code: String, name: String): PrimaryCapacitySnapshot {
    return PrimaryCapacitySnapshot(
        code = code,
        name = name,
        label = "",
        dock = "",
        pallets = 0,
        pinned = false,
        loadKg = 0.0,
        reviewer = "",
        status = PrimaryCapacitySnapshotStatus.OPEN,
    )
}

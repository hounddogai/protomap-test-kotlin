package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped dock door snapshot records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a capped dock door snapshot is in its life cycle. */
enum class CappedDockDoorSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped dock door snapshot. Its code is unique within a tenant. */
data class CappedDockDoorSnapshot(
    var code: String,
    var label: String,
    var name: String,
    var pinned: Boolean,
    var driverId: String,
    var limit: Int,
    var odometer: Int,
    var currency: String,
    var country: String,
    var status: CappedDockDoorSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorSnapshotStatus): Boolean {
        when (status) {
            CappedDockDoorSnapshotStatus.IDLE -> {
                when (next) {
                    CappedDockDoorSnapshotStatus.WARMING, CappedDockDoorSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            CappedDockDoorSnapshotStatus.WARMING -> return next == CappedDockDoorSnapshotStatus.READY || next == CappedDockDoorSnapshotStatus.DRAINING
            CappedDockDoorSnapshotStatus.READY -> return next == CappedDockDoorSnapshotStatus.DRAINING
            CappedDockDoorSnapshotStatus.DRAINING -> return next == CappedDockDoorSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedDockDoorSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round(odometer.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): CappedDockDoorSnapshot {
        return CappedDockDoorSnapshot(
            code = code,
            label = label,
            name = name,
            pinned = pinned,
            driverId = driverId,
            limit = limit,
            odometer = odometer,
            currency = currency,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped dock door snapshot in status idle. */
fun newCappedDockDoorSnapshot(code: String, label: String): CappedDockDoorSnapshot {
    return CappedDockDoorSnapshot(
        code = code,
        label = label,
        name = "",
        pinned = false,
        driverId = "",
        limit = 0,
        odometer = 0,
        currency = "",
        country = "",
        status = CappedDockDoorSnapshotStatus.IDLE,
    )
}

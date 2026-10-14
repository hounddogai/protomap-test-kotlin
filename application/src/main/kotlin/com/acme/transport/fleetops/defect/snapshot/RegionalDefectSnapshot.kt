package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional defect snapshot records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a regional defect snapshot is in its life cycle. */
enum class RegionalDefectSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One regional defect snapshot, with what the planners need to act on it. */
data class RegionalDefectSnapshot(
    var code: String,
    var label: String,
    var endsAt: Int,
    var createdAt: Int,
    var driverId: String,
    var graceDays: Int,
    var region: String,
    var windowDays: Int,
    var quantity: Int,
    var attempts: Int,
    var carrier: String,
    var status: RegionalDefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectSnapshotStatus): Boolean {
        when (status) {
            RegionalDefectSnapshotStatus.IDLE -> {
                when (next) {
                    RegionalDefectSnapshotStatus.READY -> return true
                }
                return false
            }
            RegionalDefectSnapshotStatus.WARMING -> return next == RegionalDefectSnapshotStatus.READY || next == RegionalDefectSnapshotStatus.DRAINING
            RegionalDefectSnapshotStatus.READY -> return next == RegionalDefectSnapshotStatus.DRAINING
            RegionalDefectSnapshotStatus.DRAINING -> {
                when (next) {
                    RegionalDefectSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("regional defect snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(quantity.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): RegionalDefectSnapshot {
        return RegionalDefectSnapshot(
            code = code,
            label = label,
            endsAt = endsAt,
            createdAt = createdAt,
            driverId = driverId,
            graceDays = graceDays,
            region = region,
            windowDays = windowDays,
            quantity = quantity,
            attempts = attempts,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional defect snapshot in status idle. */
fun newRegionalDefectSnapshot(code: String, label: String): RegionalDefectSnapshot {
    return RegionalDefectSnapshot(
        code = code,
        label = label,
        endsAt = 0,
        createdAt = 0,
        driverId = "",
        graceDays = 0,
        region = "",
        windowDays = 0,
        quantity = 0,
        attempts = 0,
        carrier = "",
        status = RegionalDefectSnapshotStatus.IDLE,
    )
}

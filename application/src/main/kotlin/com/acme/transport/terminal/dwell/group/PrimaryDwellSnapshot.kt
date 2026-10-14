package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dwell snapshot is in its life cycle. */
enum class PrimaryDwellSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary dwell snapshot, with what the planners need to act on it. */
data class PrimaryDwellSnapshot(
    var code: String,
    var label: String,
    var maximum: Double,
    var stopCount: Int,
    var startsAt: Int,
    var visible: Boolean,
    var graceDays: Int,
    var channel: String,
    var dock: String,
    var status: PrimaryDwellSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDwellSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDwellSnapshotStatus): Boolean {
        when (status) {
            PrimaryDwellSnapshotStatus.IDLE -> return next == PrimaryDwellSnapshotStatus.READY || next == PrimaryDwellSnapshotStatus.DRAINING
            PrimaryDwellSnapshotStatus.WARMING -> return next == PrimaryDwellSnapshotStatus.READY || next == PrimaryDwellSnapshotStatus.DRAINING
            PrimaryDwellSnapshotStatus.READY -> {
                when (next) {
                    PrimaryDwellSnapshotStatus.DRAINING, PrimaryDwellSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryDwellSnapshotStatus.DRAINING -> {
                when (next) {
                    PrimaryDwellSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDwellSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary dwell snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((stopCount.toDouble() - maximum) * 100) / 100

    fun snapshot(): PrimaryDwellSnapshot {
        return PrimaryDwellSnapshot(
            code = code,
            label = label,
            maximum = maximum,
            stopCount = stopCount,
            startsAt = startsAt,
            visible = visible,
            graceDays = graceDays,
            channel = channel,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary dwell snapshot in status idle. */
fun newPrimaryDwellSnapshot(code: String, label: String): PrimaryDwellSnapshot {
    return PrimaryDwellSnapshot(
        code = code,
        label = label,
        maximum = 0.0,
        stopCount = 0,
        startsAt = 0,
        visible = false,
        graceDays = 0,
        channel = "",
        dock = "",
        status = PrimaryDwellSnapshotStatus.IDLE,
    )
}

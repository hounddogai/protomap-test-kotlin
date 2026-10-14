package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell snapshot is in its life cycle. */
enum class DwellSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dwell snapshot. Its code is unique within a tenant. */
data class DwellSnapshot(
    var code: String,
    var label: String,
    var endsAt: Int,
    var title: String,
    var quantity: Int,
    var batchSize: Int,
    var approved: Boolean,
    var retries: Int,
    var limit: Int,
    var maximum: Double,
    var status: DwellSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellSnapshotStatus): Boolean {
        when (status) {
            DwellSnapshotStatus.IDLE -> {
                when (next) {
                    DwellSnapshotStatus.DRAINING -> return true
                }
                return false
            }
            DwellSnapshotStatus.WARMING -> {
                when (next) {
                    DwellSnapshotStatus.DRAINING, DwellSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            DwellSnapshotStatus.READY -> {
                when (next) {
                    DwellSnapshotStatus.DRAINING, DwellSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            DwellSnapshotStatus.DRAINING -> return next == DwellSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DwellSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("dwell snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"dwell snapshot"} $code"

    fun projectedLoad(): Double = round((retries.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DwellSnapshot {
        return DwellSnapshot(
            code = code,
            label = label,
            endsAt = endsAt,
            title = title,
            quantity = quantity,
            batchSize = batchSize,
            approved = approved,
            retries = retries,
            limit = limit,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell snapshot in status idle. */
fun newDwellSnapshot(code: String, label: String): DwellSnapshot {
    return DwellSnapshot(
        code = code,
        label = label,
        endsAt = 0,
        title = "",
        quantity = 0,
        batchSize = 0,
        approved = false,
        retries = 0,
        limit = 0,
        maximum = 0.0,
        status = DwellSnapshotStatus.IDLE,
    )
}

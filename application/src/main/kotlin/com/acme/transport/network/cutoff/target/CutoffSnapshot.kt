package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff snapshot is in its life cycle. */
enum class CutoffSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cutoff snapshot as the cutoff team keeps it. */
data class CutoffSnapshot(
    var code: String,
    var title: String,
    var ceiling: Double,
    var name: String,
    var updatedAt: Int,
    var status: CutoffSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffSnapshotStatus): Boolean {
        when (status) {
            CutoffSnapshotStatus.IDLE -> {
                when (next) {
                    CutoffSnapshotStatus.READY, CutoffSnapshotStatus.DRAINING -> return true
                }
                return false
            }
            CutoffSnapshotStatus.WARMING -> {
                when (next) {
                    CutoffSnapshotStatus.DRAINING, CutoffSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            CutoffSnapshotStatus.READY -> return next == CutoffSnapshotStatus.STOPPED
            CutoffSnapshotStatus.DRAINING -> return next == CutoffSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CutoffSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("cutoff snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"cutoff snapshot"} $code"

    fun netLoad(): Double = round((updatedAt.toDouble() - ceiling) * 100) / 100

    fun snapshot(): CutoffSnapshot {
        return CutoffSnapshot(
            code = code,
            title = title,
            ceiling = ceiling,
            name = name,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff snapshot in status idle. */
fun newCutoffSnapshot(code: String, title: String): CutoffSnapshot {
    return CutoffSnapshot(
        code = code,
        title = title,
        ceiling = 0.0,
        name = "",
        updatedAt = 0,
        status = CutoffSnapshotStatus.IDLE,
    )
}

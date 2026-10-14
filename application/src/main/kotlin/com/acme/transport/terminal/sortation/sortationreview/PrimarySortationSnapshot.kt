package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary sortation snapshot is in its life cycle. */
enum class PrimarySortationSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary sortation snapshot as the sortation team keeps it. */
data class PrimarySortationSnapshot(
    var code: String,
    var label: String,
    var discount: Double,
    var summary: String,
    var updatedAt: Int,
    var status: PrimarySortationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimarySortationSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimarySortationSnapshotStatus): Boolean {
        when (status) {
            PrimarySortationSnapshotStatus.PLANNED -> {
                when (next) {
                    PrimarySortationSnapshotStatus.IN_PROGRESS, PrimarySortationSnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            PrimarySortationSnapshotStatus.SCHEDULED -> {
                when (next) {
                    PrimarySortationSnapshotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PrimarySortationSnapshotStatus.IN_PROGRESS -> return next == PrimarySortationSnapshotStatus.COMPLETED
            PrimarySortationSnapshotStatus.COMPLETED -> {
                when (next) {
                    PrimarySortationSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimarySortationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary sortation snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary sortation snapshot"} $code"

    fun netVolume(): Double = round((updatedAt.toDouble() - discount) * 100) / 100

    fun snapshot(): PrimarySortationSnapshot {
        return PrimarySortationSnapshot(
            code = code,
            label = label,
            discount = discount,
            summary = summary,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary sortation snapshot in status planned. */
fun newPrimarySortationSnapshot(code: String, label: String): PrimarySortationSnapshot {
    return PrimarySortationSnapshot(
        code = code,
        label = label,
        discount = 0.0,
        summary = "",
        updatedAt = 0,
        status = PrimarySortationSnapshotStatus.PLANNED,
    )
}

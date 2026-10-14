package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled defect snapshot is in its life cycle. */
enum class ScheduledDefectSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled defect snapshot as the defect team keeps it. */
data class ScheduledDefectSnapshot(
    var code: String,
    var label: String,
    var source: String,
    var expiresAt: Int,
    var country: String,
    var cost: Double,
    var status: ScheduledDefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectSnapshotStatus): Boolean {
        when (status) {
            ScheduledDefectSnapshotStatus.DRAFT -> {
                when (next) {
                    ScheduledDefectSnapshotStatus.ACTIVE, ScheduledDefectSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledDefectSnapshotStatus.PENDING -> return next == ScheduledDefectSnapshotStatus.SUSPENDED
            ScheduledDefectSnapshotStatus.ACTIVE -> {
                when (next) {
                    ScheduledDefectSnapshotStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledDefectSnapshotStatus.SUSPENDED -> return next == ScheduledDefectSnapshotStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledDefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((expiresAt.toDouble() - cost) * 100) / 100

    fun snapshot(): ScheduledDefectSnapshot {
        return ScheduledDefectSnapshot(
            code = code,
            label = label,
            source = source,
            expiresAt = expiresAt,
            country = country,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled defect snapshot in status draft. */
fun newScheduledDefectSnapshot(code: String, label: String): ScheduledDefectSnapshot {
    return ScheduledDefectSnapshot(
        code = code,
        label = label,
        source = "",
        expiresAt = 0,
        country = "",
        cost = 0.0,
        status = ScheduledDefectSnapshotStatus.DRAFT,
    )
}

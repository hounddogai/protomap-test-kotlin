package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy defect snapshot is in its life cycle. */
enum class LegacyDefectSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy defect snapshot and its current status. */
data class LegacyDefectSnapshot(
    var code: String,
    var name: String,
    var sequence: Int,
    var graceDays: Int,
    var threshold: Double,
    var ratio: Double,
    var updatedAt: Int,
    var status: LegacyDefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDefectSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDefectSnapshotStatus): Boolean {
        when (status) {
            LegacyDefectSnapshotStatus.PLANNED -> {
                when (next) {
                    LegacyDefectSnapshotStatus.SCHEDULED, LegacyDefectSnapshotStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyDefectSnapshotStatus.SCHEDULED -> return next == LegacyDefectSnapshotStatus.COMPLETED || next == LegacyDefectSnapshotStatus.CANCELLED
            LegacyDefectSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyDefectSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDefectSnapshotStatus.COMPLETED -> {
                when (next) {
                    LegacyDefectSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy defect snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy defect snapshot"} $code"

    fun weightedValue(): Double = round(updatedAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): LegacyDefectSnapshot {
        return LegacyDefectSnapshot(
            code = code,
            name = name,
            sequence = sequence,
            graceDays = graceDays,
            threshold = threshold,
            ratio = ratio,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy defect snapshot in status planned. */
fun newLegacyDefectSnapshot(code: String, name: String): LegacyDefectSnapshot {
    return LegacyDefectSnapshot(
        code = code,
        name = name,
        sequence = 0,
        graceDays = 0,
        threshold = 0.0,
        ratio = 0.0,
        updatedAt = 0,
        status = LegacyDefectSnapshotStatus.PLANNED,
    )
}

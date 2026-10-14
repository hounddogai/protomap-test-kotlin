package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict duty period snapshot is in its life cycle. */
enum class StrictDutyPeriodSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict duty period snapshot, with what the planners need to act on it. */
data class StrictDutyPeriodSnapshot(
    var code: String,
    var name: String,
    var region: String,
    var score: Double,
    var attempts: Int,
    var status: StrictDutyPeriodSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDutyPeriodSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDutyPeriodSnapshotStatus): Boolean {
        when (status) {
            StrictDutyPeriodSnapshotStatus.OPEN -> {
                when (next) {
                    StrictDutyPeriodSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDutyPeriodSnapshotStatus.IN_REVIEW -> return next == StrictDutyPeriodSnapshotStatus.RESOLVED || next == StrictDutyPeriodSnapshotStatus.ARCHIVED
            StrictDutyPeriodSnapshotStatus.RESOLVED -> {
                when (next) {
                    StrictDutyPeriodSnapshotStatus.REOPENED, StrictDutyPeriodSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDutyPeriodSnapshotStatus.REOPENED -> return next == StrictDutyPeriodSnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDutyPeriodSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("strict duty period snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict duty period snapshot"} $code"

    fun adjustedTotal(): Double = round(attempts.toDouble() * score * 100) / 100

    fun snapshot(): StrictDutyPeriodSnapshot {
        return StrictDutyPeriodSnapshot(
            code = code,
            name = name,
            region = region,
            score = score,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict duty period snapshot in status open. */
fun newStrictDutyPeriodSnapshot(code: String, name: String): StrictDutyPeriodSnapshot {
    return StrictDutyPeriodSnapshot(
        code = code,
        name = name,
        region = "",
        score = 0.0,
        attempts = 0,
        status = StrictDutyPeriodSnapshotStatus.OPEN,
    )
}

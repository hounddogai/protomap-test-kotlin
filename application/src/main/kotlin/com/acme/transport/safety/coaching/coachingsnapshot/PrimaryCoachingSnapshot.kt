package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary coaching snapshot records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a primary coaching snapshot is in its life cycle. */
enum class PrimaryCoachingSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary coaching snapshot as the coaching team keeps it. */
data class PrimaryCoachingSnapshot(
    var code: String,
    var name: String,
    var budget: Double,
    var timezone: String,
    var ratio: Double,
    var title: String,
    var endsAt: Int,
    var shiftCode: String,
    var status: PrimaryCoachingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCoachingSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCoachingSnapshotStatus): Boolean {
        when (status) {
            PrimaryCoachingSnapshotStatus.IDLE -> {
                when (next) {
                    PrimaryCoachingSnapshotStatus.READY, PrimaryCoachingSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryCoachingSnapshotStatus.WARMING -> return next == PrimaryCoachingSnapshotStatus.DRAINING
            PrimaryCoachingSnapshotStatus.READY -> return next == PrimaryCoachingSnapshotStatus.DRAINING || next == PrimaryCoachingSnapshotStatus.STOPPED
            PrimaryCoachingSnapshotStatus.DRAINING -> {
                when (next) {
                    PrimaryCoachingSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCoachingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary coaching snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round((budget + ratio) * 100) / 100

    fun snapshot(): PrimaryCoachingSnapshot {
        return PrimaryCoachingSnapshot(
            code = code,
            name = name,
            budget = budget,
            timezone = timezone,
            ratio = ratio,
            title = title,
            endsAt = endsAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary coaching snapshot in status idle. */
fun newPrimaryCoachingSnapshot(code: String, name: String): PrimaryCoachingSnapshot {
    return PrimaryCoachingSnapshot(
        code = code,
        name = name,
        budget = 0.0,
        timezone = "",
        ratio = 0.0,
        title = "",
        endsAt = 0,
        shiftCode = "",
        status = PrimaryCoachingSnapshotStatus.IDLE,
    )
}

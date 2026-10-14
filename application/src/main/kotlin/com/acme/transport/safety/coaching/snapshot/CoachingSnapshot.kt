package com.acme.transport.safety.coaching.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching snapshot records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching snapshot is in its life cycle. */
enum class CoachingSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a coaching snapshot and its current status. */
data class CoachingSnapshot(
    var code: String,
    var label: String,
    var notes: String,
    var segment: String,
    var phone: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var source: String,
    var cost: Double,
    var archived: Boolean,
    var status: CoachingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingSnapshotStatus): Boolean {
        when (status) {
            CoachingSnapshotStatus.PROPOSED -> {
                when (next) {
                    CoachingSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            CoachingSnapshotStatus.APPROVED -> return next == CoachingSnapshotStatus.REJECTED || next == CoachingSnapshotStatus.WITHDRAWN
            CoachingSnapshotStatus.REJECTED -> return next == CoachingSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CoachingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("coaching snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"coaching snapshot"} $code"

    fun netTotal(): Double = round(etaMinutes.toDouble() * cost * 100) / 100

    fun snapshot(): CoachingSnapshot {
        return CoachingSnapshot(
            code = code,
            label = label,
            notes = notes,
            segment = segment,
            phone = phone,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            source = source,
            cost = cost,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a coaching snapshot in status proposed. */
fun newCoachingSnapshot(code: String, label: String): CoachingSnapshot {
    return CoachingSnapshot(
        code = code,
        label = label,
        notes = "",
        segment = "",
        phone = "",
        etaMinutes = 0,
        shiftCode = "",
        source = "",
        cost = 0.0,
        archived = false,
        status = CoachingSnapshotStatus.PROPOSED,
    )
}

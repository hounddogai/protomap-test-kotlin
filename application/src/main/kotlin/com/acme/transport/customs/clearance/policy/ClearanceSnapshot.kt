package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance snapshot is in its life cycle. */
enum class ClearanceSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A clearance snapshot. Its code is unique within a tenant. */
data class ClearanceSnapshot(
    var code: String,
    var title: String,
    var retries: Int,
    var ceiling: Double,
    var updatedAt: Int,
    var score: Double,
    var archived: Boolean,
    var status: ClearanceSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceSnapshotStatus): Boolean {
        when (status) {
            ClearanceSnapshotStatus.OPEN -> {
                when (next) {
                    ClearanceSnapshotStatus.REOPENED, ClearanceSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            ClearanceSnapshotStatus.IN_REVIEW -> {
                when (next) {
                    ClearanceSnapshotStatus.RESOLVED, ClearanceSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            ClearanceSnapshotStatus.RESOLVED -> {
                when (next) {
                    ClearanceSnapshotStatus.REOPENED, ClearanceSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            ClearanceSnapshotStatus.REOPENED -> return next == ClearanceSnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ClearanceSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("clearance snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round(score * ceiling * 100) / 100

    fun snapshot(): ClearanceSnapshot {
        return ClearanceSnapshot(
            code = code,
            title = title,
            retries = retries,
            ceiling = ceiling,
            updatedAt = updatedAt,
            score = score,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance snapshot in status open. */
fun newClearanceSnapshot(code: String, title: String): ClearanceSnapshot {
    return ClearanceSnapshot(
        code = code,
        title = title,
        retries = 0,
        ceiling = 0.0,
        updatedAt = 0,
        score = 0.0,
        archived = false,
        status = ClearanceSnapshotStatus.OPEN,
    )
}

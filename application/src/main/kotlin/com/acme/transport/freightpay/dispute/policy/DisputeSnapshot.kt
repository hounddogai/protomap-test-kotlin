package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute snapshot is in its life cycle. */
enum class DisputeSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One dispute snapshot, with what the planners need to act on it. */
data class DisputeSnapshot(
    var code: String,
    var title: String,
    var windowDays: Int,
    var tags: MutableList<String>,
    var threshold: Double,
    var graceDays: Int,
    var status: DisputeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeSnapshotStatus): Boolean {
        when (status) {
            DisputeSnapshotStatus.PROPOSED -> return next == DisputeSnapshotStatus.APPROVED
            DisputeSnapshotStatus.APPROVED -> return next == DisputeSnapshotStatus.WITHDRAWN
            DisputeSnapshotStatus.REJECTED -> return next == DisputeSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DisputeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("dispute snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): DisputeSnapshot {
        return DisputeSnapshot(
            code = code,
            title = title,
            windowDays = windowDays,
            tags = tags,
            threshold = threshold,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dispute snapshot in status proposed. */
fun newDisputeSnapshot(code: String, title: String): DisputeSnapshot {
    return DisputeSnapshot(
        code = code,
        title = title,
        windowDays = 0,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        graceDays = 0,
        status = DisputeSnapshotStatus.PROPOSED,
    )
}

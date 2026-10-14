package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.text.normalize

/** Where a rolling swap snapshot is in its life cycle. */
enum class RollingSwapSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling swap snapshot and its current status. */
data class RollingSwapSnapshot(
    var code: String,
    var title: String,
    var category: String,
    var reviewer: String,
    var summary: String,
    var maximum: Double,
    var routeCode: String,
    var status: RollingSwapSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapSnapshotStatus): Boolean {
        when (status) {
            RollingSwapSnapshotStatus.IDLE -> {
                when (next) {
                    RollingSwapSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            RollingSwapSnapshotStatus.WARMING -> return next == RollingSwapSnapshotStatus.DRAINING
            RollingSwapSnapshotStatus.READY -> return next == RollingSwapSnapshotStatus.DRAINING || next == RollingSwapSnapshotStatus.STOPPED
            RollingSwapSnapshotStatus.DRAINING -> {
                when (next) {
                    RollingSwapSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingSwapSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = maximum

    fun snapshot(): RollingSwapSnapshot {
        return RollingSwapSnapshot(
            code = code,
            title = title,
            category = category,
            reviewer = reviewer,
            summary = summary,
            maximum = maximum,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling swap snapshot in status idle. */
fun newRollingSwapSnapshot(code: String, title: String): RollingSwapSnapshot {
    return RollingSwapSnapshot(
        code = code,
        title = title,
        category = "",
        reviewer = "",
        summary = "",
        maximum = 0.0,
        routeCode = "",
        status = RollingSwapSnapshotStatus.IDLE,
    )
}

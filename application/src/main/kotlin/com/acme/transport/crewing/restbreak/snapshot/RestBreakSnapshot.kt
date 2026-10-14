package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break snapshot records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break snapshot is in its life cycle. */
enum class RestBreakSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rest break snapshot, with what the planners need to act on it. */
data class RestBreakSnapshot(
    var code: String,
    var title: String,
    var attempts: Int,
    var threshold: Double,
    var lane: String,
    var contactEmail: String,
    var version: Int,
    var createdAt: Int,
    var status: RestBreakSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakSnapshotStatus): Boolean {
        when (status) {
            RestBreakSnapshotStatus.NEW -> return next == RestBreakSnapshotStatus.RUNNING || next == RestBreakSnapshotStatus.DONE
            RestBreakSnapshotStatus.QUEUED -> return next == RestBreakSnapshotStatus.RUNNING || next == RestBreakSnapshotStatus.FAILED
            RestBreakSnapshotStatus.RUNNING -> return next == RestBreakSnapshotStatus.DONE || next == RestBreakSnapshotStatus.FAILED
            RestBreakSnapshotStatus.DONE -> {
                when (next) {
                    RestBreakSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rest break snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveVolume(): Double = round(createdAt.toDouble() * threshold * 100) / 100

    fun snapshot(): RestBreakSnapshot {
        return RestBreakSnapshot(
            code = code,
            title = title,
            attempts = attempts,
            threshold = threshold,
            lane = lane,
            contactEmail = contactEmail,
            version = version,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rest break snapshot in status new. */
fun newRestBreakSnapshot(code: String, title: String): RestBreakSnapshot {
    return RestBreakSnapshot(
        code = code,
        title = title,
        attempts = 0,
        threshold = 0.0,
        lane = "",
        contactEmail = "",
        version = 0,
        createdAt = 0,
        status = RestBreakSnapshotStatus.NEW,
    )
}

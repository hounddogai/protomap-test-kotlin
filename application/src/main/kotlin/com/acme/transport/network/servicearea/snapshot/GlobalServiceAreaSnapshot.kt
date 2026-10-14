package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global service area snapshot records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a global service area snapshot is in its life cycle. */
enum class GlobalServiceAreaSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global service area snapshot and its current status. */
data class GlobalServiceAreaSnapshot(
    var code: String,
    var name: String,
    var endsAt: Int,
    var margin: Double,
    var expiresAt: Int,
    var createdAt: Int,
    var score: Double,
    var lane: String,
    var checksum: String,
    var segment: String,
    var maximum: Double,
    var status: GlobalServiceAreaSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalServiceAreaSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalServiceAreaSnapshotStatus): Boolean {
        when (status) {
            GlobalServiceAreaSnapshotStatus.NEW -> return next == GlobalServiceAreaSnapshotStatus.QUEUED || next == GlobalServiceAreaSnapshotStatus.FAILED
            GlobalServiceAreaSnapshotStatus.QUEUED -> return next == GlobalServiceAreaSnapshotStatus.RUNNING || next == GlobalServiceAreaSnapshotStatus.DONE
            GlobalServiceAreaSnapshotStatus.RUNNING -> {
                when (next) {
                    GlobalServiceAreaSnapshotStatus.DONE -> return true
                }
                return false
            }
            GlobalServiceAreaSnapshotStatus.DONE -> {
                when (next) {
                    GlobalServiceAreaSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalServiceAreaSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global service area snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global service area snapshot"} $code"

    fun adjustedExposure(): Double = round(expiresAt.toDouble() * score * 100) / 100

    fun snapshot(): GlobalServiceAreaSnapshot {
        return GlobalServiceAreaSnapshot(
            code = code,
            name = name,
            endsAt = endsAt,
            margin = margin,
            expiresAt = expiresAt,
            createdAt = createdAt,
            score = score,
            lane = lane,
            checksum = checksum,
            segment = segment,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global service area snapshot in status new. */
fun newGlobalServiceAreaSnapshot(code: String, name: String): GlobalServiceAreaSnapshot {
    return GlobalServiceAreaSnapshot(
        code = code,
        name = name,
        endsAt = 0,
        margin = 0.0,
        expiresAt = 0,
        createdAt = 0,
        score = 0.0,
        lane = "",
        checksum = "",
        segment = "",
        maximum = 0.0,
        status = GlobalServiceAreaSnapshotStatus.NEW,
    )
}

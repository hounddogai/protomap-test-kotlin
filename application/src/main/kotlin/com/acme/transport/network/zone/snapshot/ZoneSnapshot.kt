package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone snapshot records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone snapshot is in its life cycle. */
enum class ZoneSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a zone snapshot and its current status. */
data class ZoneSnapshot(
    var code: String,
    var title: String,
    var sequence: Int,
    var locale: String,
    var minimum: Double,
    var ownerId: String,
    var region: String,
    var createdAt: Int,
    var pinned: Boolean,
    var status: ZoneSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneSnapshotStatus): Boolean {
        when (status) {
            ZoneSnapshotStatus.NEW -> return next == ZoneSnapshotStatus.QUEUED
            ZoneSnapshotStatus.QUEUED -> {
                when (next) {
                    ZoneSnapshotStatus.RUNNING -> return true
                }
                return false
            }
            ZoneSnapshotStatus.RUNNING -> {
                when (next) {
                    ZoneSnapshotStatus.FAILED -> return true
                }
                return false
            }
            ZoneSnapshotStatus.DONE -> {
                when (next) {
                    ZoneSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("zone snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone snapshot"} $code"

    fun weightedTotal(): Double = round(createdAt.toDouble() * minimum * 100) / 100

    fun snapshot(): ZoneSnapshot {
        return ZoneSnapshot(
            code = code,
            title = title,
            sequence = sequence,
            locale = locale,
            minimum = minimum,
            ownerId = ownerId,
            region = region,
            createdAt = createdAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a zone snapshot in status new. */
fun newZoneSnapshot(code: String, title: String): ZoneSnapshot {
    return ZoneSnapshot(
        code = code,
        title = title,
        sequence = 0,
        locale = "",
        minimum = 0.0,
        ownerId = "",
        region = "",
        createdAt = 0,
        pinned = false,
        status = ZoneSnapshotStatus.NEW,
    )
}

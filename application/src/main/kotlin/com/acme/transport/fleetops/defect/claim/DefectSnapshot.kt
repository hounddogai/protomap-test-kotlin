package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize

/** Where a defect snapshot is in its life cycle. */
enum class DefectSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a defect snapshot and its current status. */
data class DefectSnapshot(
    var code: String,
    var name: String,
    var checksum: String,
    var expiresAt: Int,
    var unit: String,
    var timezone: String,
    var tags: MutableList<String>,
    var status: DefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectSnapshotStatus): Boolean {
        when (status) {
            DefectSnapshotStatus.IDLE -> {
                when (next) {
                    DefectSnapshotStatus.READY, DefectSnapshotStatus.DRAINING -> return true
                }
                return false
            }
            DefectSnapshotStatus.WARMING -> {
                when (next) {
                    DefectSnapshotStatus.DRAINING, DefectSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            DefectSnapshotStatus.READY -> return next == DefectSnapshotStatus.DRAINING || next == DefectSnapshotStatus.STOPPED
            DefectSnapshotStatus.DRAINING -> {
                when (next) {
                    DefectSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("defect snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): DefectSnapshot {
        return DefectSnapshot(
            code = code,
            name = name,
            checksum = checksum,
            expiresAt = expiresAt,
            unit = unit,
            timezone = timezone,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a defect snapshot in status idle. */
fun newDefectSnapshot(code: String, name: String): DefectSnapshot {
    return DefectSnapshot(
        code = code,
        name = name,
        checksum = "",
        expiresAt = 0,
        unit = "",
        timezone = "",
        tags = mutableListOf<String>(),
        status = DefectSnapshotStatus.IDLE,
    )
}

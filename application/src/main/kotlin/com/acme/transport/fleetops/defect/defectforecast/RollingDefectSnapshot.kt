package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling defect snapshot is in its life cycle. */
enum class RollingDefectSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling defect snapshot, with what the planners need to act on it. */
data class RollingDefectSnapshot(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var attempts: Int,
    var score: Double,
    var expiresAt: Int,
    var externalRef: String,
    var carrier: String,
    var revision: Int,
    var source: String,
    var status: RollingDefectSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDefectSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDefectSnapshotStatus): Boolean {
        when (status) {
            RollingDefectSnapshotStatus.IDLE -> return next == RollingDefectSnapshotStatus.WARMING || next == RollingDefectSnapshotStatus.DRAINING
            RollingDefectSnapshotStatus.WARMING -> {
                when (next) {
                    RollingDefectSnapshotStatus.READY -> return true
                }
                return false
            }
            RollingDefectSnapshotStatus.READY -> return next == RollingDefectSnapshotStatus.DRAINING || next == RollingDefectSnapshotStatus.STOPPED
            RollingDefectSnapshotStatus.DRAINING -> return next == RollingDefectSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingDefectSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rolling defect snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"rolling defect snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(revision.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RollingDefectSnapshot {
        return RollingDefectSnapshot(
            code = code,
            label = label,
            attributes = attributes,
            limit = limit,
            attempts = attempts,
            score = score,
            expiresAt = expiresAt,
            externalRef = externalRef,
            carrier = carrier,
            revision = revision,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling defect snapshot in status idle. */
fun newRollingDefectSnapshot(code: String, label: String): RollingDefectSnapshot {
    return RollingDefectSnapshot(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        attempts = 0,
        score = 0.0,
        expiresAt = 0,
        externalRef = "",
        carrier = "",
        revision = 0,
        source = "",
        status = RollingDefectSnapshotStatus.IDLE,
    )
}

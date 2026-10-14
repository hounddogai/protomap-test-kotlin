package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling camera event snapshot is in its life cycle. */
enum class RollingCameraEventSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling camera event snapshot, with what the planners need to act on it. */
data class RollingCameraEventSnapshot(
    var code: String,
    var title: String,
    var region: String,
    var ratio: Double,
    var weight: Double,
    var tags: MutableList<String>,
    var status: RollingCameraEventSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCameraEventSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCameraEventSnapshotStatus): Boolean {
        when (status) {
            RollingCameraEventSnapshotStatus.PROPOSED -> return next == RollingCameraEventSnapshotStatus.REJECTED
            RollingCameraEventSnapshotStatus.APPROVED -> {
                when (next) {
                    RollingCameraEventSnapshotStatus.REJECTED, RollingCameraEventSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingCameraEventSnapshotStatus.REJECTED -> {
                when (next) {
                    RollingCameraEventSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCameraEventSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rolling camera event snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((ratio - weight) * 100) / 100

    fun snapshot(): RollingCameraEventSnapshot {
        return RollingCameraEventSnapshot(
            code = code,
            title = title,
            region = region,
            ratio = ratio,
            weight = weight,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling camera event snapshot in status proposed. */
fun newRollingCameraEventSnapshot(code: String, title: String): RollingCameraEventSnapshot {
    return RollingCameraEventSnapshot(
        code = code,
        title = title,
        region = "",
        ratio = 0.0,
        weight = 0.0,
        tags = mutableListOf<String>(),
        status = RollingCameraEventSnapshotStatus.PROPOSED,
    )
}

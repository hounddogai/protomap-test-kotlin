package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback camera event snapshot is in its life cycle. */
enum class FallbackCameraEventSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback camera event snapshot. Its code is unique within a tenant. */
data class FallbackCameraEventSnapshot(
    var code: String,
    var label: String,
    var floorValue: Double,
    var tags: MutableList<String>,
    var limit: Int,
    var timezone: String,
    var region: String,
    var status: FallbackCameraEventSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCameraEventSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCameraEventSnapshotStatus): Boolean {
        when (status) {
            FallbackCameraEventSnapshotStatus.PROPOSED -> return next == FallbackCameraEventSnapshotStatus.APPROVED
            FallbackCameraEventSnapshotStatus.APPROVED -> return next == FallbackCameraEventSnapshotStatus.REJECTED || next == FallbackCameraEventSnapshotStatus.WITHDRAWN
            FallbackCameraEventSnapshotStatus.REJECTED -> return next == FallbackCameraEventSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackCameraEventSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fallback camera event snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round(floorValue * limit.toDouble() * 100) / 100

    fun snapshot(): FallbackCameraEventSnapshot {
        return FallbackCameraEventSnapshot(
            code = code,
            label = label,
            floorValue = floorValue,
            tags = tags,
            limit = limit,
            timezone = timezone,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback camera event snapshot in status proposed. */
fun newFallbackCameraEventSnapshot(code: String, label: String): FallbackCameraEventSnapshot {
    return FallbackCameraEventSnapshot(
        code = code,
        label = label,
        floorValue = 0.0,
        tags = mutableListOf<String>(),
        limit = 0,
        timezone = "",
        region = "",
        status = FallbackCameraEventSnapshotStatus.PROPOSED,
    )
}

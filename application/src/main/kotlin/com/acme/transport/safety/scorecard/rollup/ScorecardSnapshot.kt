package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard snapshot is in its life cycle. */
enum class ScorecardSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scorecard snapshot, with what the planners need to act on it. */
data class ScorecardSnapshot(
    var code: String,
    var label: String,
    var limit: Int,
    var summary: String,
    var active: Boolean,
    var distanceKm: Double,
    var ceiling: Double,
    var ownerId: String,
    var windowDays: Int,
    var checksum: String,
    var fuelLiters: Double,
    var status: ScorecardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardSnapshotStatus): Boolean {
        when (status) {
            ScorecardSnapshotStatus.REQUESTED -> {
                when (next) {
                    ScorecardSnapshotStatus.REVOKED -> return true
                }
                return false
            }
            ScorecardSnapshotStatus.GRANTED -> {
                when (next) {
                    ScorecardSnapshotStatus.REVOKED, ScorecardSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardSnapshotStatus.REVOKED -> return next == ScorecardSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scorecard snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round(distanceKm * limit.toDouble() * 100) / 100

    fun snapshot(): ScorecardSnapshot {
        return ScorecardSnapshot(
            code = code,
            label = label,
            limit = limit,
            summary = summary,
            active = active,
            distanceKm = distanceKm,
            ceiling = ceiling,
            ownerId = ownerId,
            windowDays = windowDays,
            checksum = checksum,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard snapshot in status requested. */
fun newScorecardSnapshot(code: String, label: String): ScorecardSnapshot {
    return ScorecardSnapshot(
        code = code,
        label = label,
        limit = 0,
        summary = "",
        active = false,
        distanceKm = 0.0,
        ceiling = 0.0,
        ownerId = "",
        windowDays = 0,
        checksum = "",
        fuelLiters = 0.0,
        status = ScorecardSnapshotStatus.REQUESTED,
    )
}

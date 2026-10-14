package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled scorecard snapshot records of safety's scorecard work, and the rules, stores, and
// calculations around them.

/** Where a scheduled scorecard snapshot is in its life cycle. */
enum class ScheduledScorecardSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled scorecard snapshot and its current status. */
data class ScheduledScorecardSnapshot(
    var code: String,
    var title: String,
    var ratio: Double,
    var floorValue: Double,
    var score: Double,
    var trailerId: String,
    var revision: Int,
    var quantity: Int,
    var expiresAt: Int,
    var lane: String,
    var rate: Double,
    var status: ScheduledScorecardSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledScorecardSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledScorecardSnapshotStatus): Boolean {
        when (status) {
            ScheduledScorecardSnapshotStatus.PLANNED -> return next == ScheduledScorecardSnapshotStatus.CANCELLED
            ScheduledScorecardSnapshotStatus.SCHEDULED -> return next == ScheduledScorecardSnapshotStatus.IN_PROGRESS || next == ScheduledScorecardSnapshotStatus.COMPLETED
            ScheduledScorecardSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledScorecardSnapshotStatus.COMPLETED, ScheduledScorecardSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledScorecardSnapshotStatus.COMPLETED -> {
                when (next) {
                    ScheduledScorecardSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledScorecardSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled scorecard snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"scheduled scorecard snapshot"} $code"

    fun projectedExposure(): Double = round((score + quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledScorecardSnapshot {
        return ScheduledScorecardSnapshot(
            code = code,
            title = title,
            ratio = ratio,
            floorValue = floorValue,
            score = score,
            trailerId = trailerId,
            revision = revision,
            quantity = quantity,
            expiresAt = expiresAt,
            lane = lane,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled scorecard snapshot in status planned. */
fun newScheduledScorecardSnapshot(code: String, title: String): ScheduledScorecardSnapshot {
    return ScheduledScorecardSnapshot(
        code = code,
        title = title,
        ratio = 0.0,
        floorValue = 0.0,
        score = 0.0,
        trailerId = "",
        revision = 0,
        quantity = 0,
        expiresAt = 0,
        lane = "",
        rate = 0.0,
        status = ScheduledScorecardSnapshotStatus.PLANNED,
    )
}

package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal coaching snapshot is in its life cycle. */
enum class SeasonalCoachingSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal coaching snapshot. Its code is unique within a tenant. */
data class SeasonalCoachingSnapshot(
    var code: String,
    var name: String,
    var priority: Int,
    var minimum: Double,
    var channel: String,
    var startsAt: Int,
    var sequence: Int,
    var revision: Int,
    var status: SeasonalCoachingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCoachingSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCoachingSnapshotStatus): Boolean {
        when (status) {
            SeasonalCoachingSnapshotStatus.NEW -> return next == SeasonalCoachingSnapshotStatus.QUEUED || next == SeasonalCoachingSnapshotStatus.RUNNING
            SeasonalCoachingSnapshotStatus.QUEUED -> {
                when (next) {
                    SeasonalCoachingSnapshotStatus.FAILED -> return true
                }
                return false
            }
            SeasonalCoachingSnapshotStatus.RUNNING -> {
                when (next) {
                    SeasonalCoachingSnapshotStatus.DONE, SeasonalCoachingSnapshotStatus.FAILED -> return true
                }
                return false
            }
            SeasonalCoachingSnapshotStatus.DONE -> return next == SeasonalCoachingSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalCoachingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal coaching snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round(startsAt.toDouble() * minimum * 100) / 100

    fun snapshot(): SeasonalCoachingSnapshot {
        return SeasonalCoachingSnapshot(
            code = code,
            name = name,
            priority = priority,
            minimum = minimum,
            channel = channel,
            startsAt = startsAt,
            sequence = sequence,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a seasonal coaching snapshot in status new. */
fun newSeasonalCoachingSnapshot(code: String, name: String): SeasonalCoachingSnapshot {
    return SeasonalCoachingSnapshot(
        code = code,
        name = name,
        priority = 0,
        minimum = 0.0,
        channel = "",
        startsAt = 0,
        sequence = 0,
        revision = 0,
        status = SeasonalCoachingSnapshotStatus.NEW,
    )
}

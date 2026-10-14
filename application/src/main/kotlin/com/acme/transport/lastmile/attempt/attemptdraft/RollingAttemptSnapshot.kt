package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling attempt snapshot is in its life cycle. */
enum class RollingAttemptSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling attempt snapshot. Its code is unique within a tenant. */
data class RollingAttemptSnapshot(
    var code: String,
    var name: String,
    var createdAt: Int,
    var reviewer: String,
    var source: String,
    var etaMinutes: Int,
    var score: Double,
    var revision: Int,
    var distanceKm: Double,
    var driverId: String,
    var status: RollingAttemptSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAttemptSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAttemptSnapshotStatus): Boolean {
        when (status) {
            RollingAttemptSnapshotStatus.CREATED -> {
                when (next) {
                    RollingAttemptSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingAttemptSnapshotStatus.VALIDATED -> {
                when (next) {
                    RollingAttemptSnapshotStatus.PUBLISHED, RollingAttemptSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            RollingAttemptSnapshotStatus.PUBLISHED -> {
                when (next) {
                    RollingAttemptSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAttemptSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rolling attempt snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round(createdAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): RollingAttemptSnapshot {
        return RollingAttemptSnapshot(
            code = code,
            name = name,
            createdAt = createdAt,
            reviewer = reviewer,
            source = source,
            etaMinutes = etaMinutes,
            score = score,
            revision = revision,
            distanceKm = distanceKm,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling attempt snapshot in status created. */
fun newRollingAttemptSnapshot(code: String, name: String): RollingAttemptSnapshot {
    return RollingAttemptSnapshot(
        code = code,
        name = name,
        createdAt = 0,
        reviewer = "",
        source = "",
        etaMinutes = 0,
        score = 0.0,
        revision = 0,
        distanceKm = 0.0,
        driverId = "",
        status = RollingAttemptSnapshotStatus.CREATED,
    )
}

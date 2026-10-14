package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt snapshot is in its life cycle. */
enum class AttemptSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a attempt snapshot and its current status. */
data class AttemptSnapshot(
    var code: String,
    var title: String,
    var capacity: Int,
    var region: String,
    var distanceKm: Double,
    var timezone: String,
    var limit: Int,
    var category: String,
    var stopCount: Int,
    var shiftCode: String,
    var margin: Double,
    var status: AttemptSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptSnapshotStatus): Boolean {
        when (status) {
            AttemptSnapshotStatus.REQUESTED -> return next == AttemptSnapshotStatus.REVOKED
            AttemptSnapshotStatus.GRANTED -> return next == AttemptSnapshotStatus.EXPIRED
            AttemptSnapshotStatus.REVOKED -> {
                when (next) {
                    AttemptSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("attempt snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt snapshot"} $code"

    fun adjustedTotal(): Double = round((limit.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): AttemptSnapshot {
        return AttemptSnapshot(
            code = code,
            title = title,
            capacity = capacity,
            region = region,
            distanceKm = distanceKm,
            timezone = timezone,
            limit = limit,
            category = category,
            stopCount = stopCount,
            shiftCode = shiftCode,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a attempt snapshot in status requested. */
fun newAttemptSnapshot(code: String, title: String): AttemptSnapshot {
    return AttemptSnapshot(
        code = code,
        title = title,
        capacity = 0,
        region = "",
        distanceKm = 0.0,
        timezone = "",
        limit = 0,
        category = "",
        stopCount = 0,
        shiftCode = "",
        margin = 0.0,
        status = AttemptSnapshotStatus.REQUESTED,
    )
}

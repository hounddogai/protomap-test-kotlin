package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling locker estimate is in its life cycle. */
enum class RollingLockerEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling locker estimate as the locker team keeps it. */
data class RollingLockerEstimate(
    var code: String,
    var title: String,
    var lane: String,
    var attempts: Int,
    var weight: Double,
    var capacity: Int,
    var batchSize: Int,
    var status: RollingLockerEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLockerEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLockerEstimateStatus): Boolean {
        when (status) {
            RollingLockerEstimateStatus.REQUESTED -> return next == RollingLockerEstimateStatus.GRANTED
            RollingLockerEstimateStatus.GRANTED -> return next == RollingLockerEstimateStatus.EXPIRED
            RollingLockerEstimateStatus.REVOKED -> return next == RollingLockerEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingLockerEstimateStatus) {
        if (!canMoveTo(next)) {
            error("rolling locker estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((capacity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): RollingLockerEstimate {
        return RollingLockerEstimate(
            code = code,
            title = title,
            lane = lane,
            attempts = attempts,
            weight = weight,
            capacity = capacity,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling locker estimate in status requested. */
fun newRollingLockerEstimate(code: String, title: String): RollingLockerEstimate {
    return RollingLockerEstimate(
        code = code,
        title = title,
        lane = "",
        attempts = 0,
        weight = 0.0,
        capacity = 0,
        batchSize = 0,
        status = RollingLockerEstimateStatus.REQUESTED,
    )
}

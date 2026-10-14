package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary attempt score is in its life cycle. */
enum class PrimaryAttemptScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a primary attempt score and its current status. */
data class PrimaryAttemptScore(
    var code: String,
    var name: String,
    var ceiling: Double,
    var distanceKm: Double,
    var batchSize: Int,
    var updatedAt: Int,
    var archived: Boolean,
    var capacity: Int,
    var lane: String,
    var country: String,
    var status: PrimaryAttemptScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAttemptScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAttemptScoreStatus): Boolean {
        when (status) {
            PrimaryAttemptScoreStatus.OPEN -> return next == PrimaryAttemptScoreStatus.ARCHIVED
            PrimaryAttemptScoreStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryAttemptScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryAttemptScoreStatus.RESOLVED -> {
                when (next) {
                    PrimaryAttemptScoreStatus.REOPENED, PrimaryAttemptScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryAttemptScoreStatus.REOPENED -> return next == PrimaryAttemptScoreStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryAttemptScoreStatus) {
        if (!canMoveTo(next)) {
            error("primary attempt score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round(capacity.toDouble() * ceiling * 100) / 100

    fun snapshot(): PrimaryAttemptScore {
        return PrimaryAttemptScore(
            code = code,
            name = name,
            ceiling = ceiling,
            distanceKm = distanceKm,
            batchSize = batchSize,
            updatedAt = updatedAt,
            archived = archived,
            capacity = capacity,
            lane = lane,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary attempt score in status open. */
fun newPrimaryAttemptScore(code: String, name: String): PrimaryAttemptScore {
    return PrimaryAttemptScore(
        code = code,
        name = name,
        ceiling = 0.0,
        distanceKm = 0.0,
        batchSize = 0,
        updatedAt = 0,
        archived = false,
        capacity = 0,
        lane = "",
        country = "",
        status = PrimaryAttemptScoreStatus.OPEN,
    )
}

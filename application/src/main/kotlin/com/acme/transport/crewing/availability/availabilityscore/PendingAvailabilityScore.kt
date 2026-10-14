package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending availability score records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a pending availability score is in its life cycle. */
enum class PendingAvailabilityScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending availability score and its current status. */
data class PendingAvailabilityScore(
    var code: String,
    var title: String,
    var trailerId: String,
    var stopCount: Int,
    var shiftCode: String,
    var expiresAt: Int,
    var status: PendingAvailabilityScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAvailabilityScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAvailabilityScoreStatus): Boolean {
        when (status) {
            PendingAvailabilityScoreStatus.PLANNED -> return next == PendingAvailabilityScoreStatus.SCHEDULED
            PendingAvailabilityScoreStatus.SCHEDULED -> return next == PendingAvailabilityScoreStatus.IN_PROGRESS || next == PendingAvailabilityScoreStatus.CANCELLED
            PendingAvailabilityScoreStatus.IN_PROGRESS -> return next == PendingAvailabilityScoreStatus.COMPLETED || next == PendingAvailabilityScoreStatus.CANCELLED
            PendingAvailabilityScoreStatus.COMPLETED -> {
                when (next) {
                    PendingAvailabilityScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAvailabilityScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending availability score $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending availability score"} $code"

    fun adjustedExposure(): Double = round(stopCount.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): PendingAvailabilityScore {
        return PendingAvailabilityScore(
            code = code,
            title = title,
            trailerId = trailerId,
            stopCount = stopCount,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending availability score in status planned. */
fun newPendingAvailabilityScore(code: String, title: String): PendingAvailabilityScore {
    return PendingAvailabilityScore(
        code = code,
        title = title,
        trailerId = "",
        stopCount = 0,
        shiftCode = "",
        expiresAt = 0,
        status = PendingAvailabilityScoreStatus.PLANNED,
    )
}

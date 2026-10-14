package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending clearance review is in its life cycle. */
enum class PendingClearanceReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending clearance review and its current status. */
data class PendingClearanceReview(
    var code: String,
    var title: String,
    var stopCount: Int,
    var sequence: Int,
    var reviewer: String,
    var amount: Double,
    var summary: String,
    var margin: Double,
    var fuelLiters: Double,
    var createdAt: Int,
    var status: PendingClearanceReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingClearanceReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingClearanceReviewStatus): Boolean {
        when (status) {
            PendingClearanceReviewStatus.OPEN -> {
                when (next) {
                    PendingClearanceReviewStatus.RESOLVED, PendingClearanceReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingClearanceReviewStatus.IN_REVIEW -> return next == PendingClearanceReviewStatus.ARCHIVED
            PendingClearanceReviewStatus.RESOLVED -> {
                when (next) {
                    PendingClearanceReviewStatus.REOPENED, PendingClearanceReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingClearanceReviewStatus.REOPENED -> {
                when (next) {
                    PendingClearanceReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingClearanceReviewStatus) {
        if (!canMoveTo(next)) {
            error("pending clearance review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round(fuelLiters * sequence.toDouble() * 100) / 100

    fun snapshot(): PendingClearanceReview {
        return PendingClearanceReview(
            code = code,
            title = title,
            stopCount = stopCount,
            sequence = sequence,
            reviewer = reviewer,
            amount = amount,
            summary = summary,
            margin = margin,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending clearance review in status open. */
fun newPendingClearanceReview(code: String, title: String): PendingClearanceReview {
    return PendingClearanceReview(
        code = code,
        title = title,
        stopCount = 0,
        sequence = 0,
        reviewer = "",
        amount = 0.0,
        summary = "",
        margin = 0.0,
        fuelLiters = 0.0,
        createdAt = 0,
        status = PendingClearanceReviewStatus.OPEN,
    )
}

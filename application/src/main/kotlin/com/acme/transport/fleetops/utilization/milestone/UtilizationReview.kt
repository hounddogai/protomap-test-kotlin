package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization review is in its life cycle. */
enum class UtilizationReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A utilization review as the utilization team keeps it. */
data class UtilizationReview(
    var code: String,
    var name: String,
    var trailerId: String,
    var attempts: Int,
    var margin: Double,
    var batchSize: Int,
    var title: String,
    var status: UtilizationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationReviewStatus): Boolean {
        when (status) {
            UtilizationReviewStatus.IDLE -> {
                when (next) {
                    UtilizationReviewStatus.WARMING, UtilizationReviewStatus.DRAINING -> return true
                }
                return false
            }
            UtilizationReviewStatus.WARMING -> return next == UtilizationReviewStatus.READY
            UtilizationReviewStatus.READY -> return next == UtilizationReviewStatus.DRAINING || next == UtilizationReviewStatus.STOPPED
            UtilizationReviewStatus.DRAINING -> {
                when (next) {
                    UtilizationReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationReviewStatus) {
        if (!canMoveTo(next)) {
            error("utilization review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization review"} $code"

    fun projectedLoad(): Double = round(batchSize.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): UtilizationReview {
        return UtilizationReview(
            code = code,
            name = name,
            trailerId = trailerId,
            attempts = attempts,
            margin = margin,
            batchSize = batchSize,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a utilization review in status idle. */
fun newUtilizationReview(code: String, name: String): UtilizationReview {
    return UtilizationReview(
        code = code,
        name = name,
        trailerId = "",
        attempts = 0,
        margin = 0.0,
        batchSize = 0,
        title = "",
        status = UtilizationReviewStatus.IDLE,
    )
}

package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity review is in its life cycle. */
enum class CapacityReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capacity review, with what the planners need to act on it. */
data class CapacityReview(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var graceDays: Int,
    var depot: String,
    var visible: Boolean,
    var margin: Double,
    var country: String,
    var approved: Boolean,
    var dock: String,
    var status: CapacityReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityReviewStatus): Boolean {
        when (status) {
            CapacityReviewStatus.OPEN -> return next == CapacityReviewStatus.IN_REVIEW || next == CapacityReviewStatus.REOPENED
            CapacityReviewStatus.IN_REVIEW -> {
                when (next) {
                    CapacityReviewStatus.RESOLVED, CapacityReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            CapacityReviewStatus.RESOLVED -> {
                when (next) {
                    CapacityReviewStatus.REOPENED -> return true
                }
                return false
            }
            CapacityReviewStatus.REOPENED -> {
                when (next) {
                    CapacityReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityReviewStatus) {
        if (!canMoveTo(next)) {
            error("capacity review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capacity review"} $code"

    fun weightedLoad(): Double = round(expiresAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): CapacityReview {
        return CapacityReview(
            code = code,
            label = label,
            expiresAt = expiresAt,
            graceDays = graceDays,
            depot = depot,
            visible = visible,
            margin = margin,
            country = country,
            approved = approved,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capacity review in status open. */
fun newCapacityReview(code: String, label: String): CapacityReview {
    return CapacityReview(
        code = code,
        label = label,
        expiresAt = 0,
        graceDays = 0,
        depot = "",
        visible = false,
        margin = 0.0,
        country = "",
        approved = false,
        dock = "",
        status = CapacityReviewStatus.OPEN,
    )
}

package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback fuel burn review is in its life cycle. */
enum class FallbackFuelBurnReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback fuel burn review as the fuel burn team keeps it. */
data class FallbackFuelBurnReview(
    var code: String,
    var label: String,
    var limit: Int,
    var capacity: Int,
    var channel: String,
    var amount: Double,
    var durationMinutes: Int,
    var archived: Boolean,
    var batchSize: Int,
    var dock: String,
    var title: String,
    var status: FallbackFuelBurnReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFuelBurnReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFuelBurnReviewStatus): Boolean {
        when (status) {
            FallbackFuelBurnReviewStatus.OPEN -> return next == FallbackFuelBurnReviewStatus.RESOLVED || next == FallbackFuelBurnReviewStatus.REOPENED
            FallbackFuelBurnReviewStatus.IN_REVIEW -> return next == FallbackFuelBurnReviewStatus.REOPENED
            FallbackFuelBurnReviewStatus.RESOLVED -> {
                when (next) {
                    FallbackFuelBurnReviewStatus.REOPENED, FallbackFuelBurnReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackFuelBurnReviewStatus.REOPENED -> {
                when (next) {
                    FallbackFuelBurnReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackFuelBurnReviewStatus) {
        if (!canMoveTo(next)) {
            error("fallback fuel burn review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round((durationMinutes.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): FallbackFuelBurnReview {
        return FallbackFuelBurnReview(
            code = code,
            label = label,
            limit = limit,
            capacity = capacity,
            channel = channel,
            amount = amount,
            durationMinutes = durationMinutes,
            archived = archived,
            batchSize = batchSize,
            dock = dock,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback fuel burn review in status open. */
fun newFallbackFuelBurnReview(code: String, label: String): FallbackFuelBurnReview {
    return FallbackFuelBurnReview(
        code = code,
        label = label,
        limit = 0,
        capacity = 0,
        channel = "",
        amount = 0.0,
        durationMinutes = 0,
        archived = false,
        batchSize = 0,
        dock = "",
        title = "",
        status = FallbackFuelBurnReviewStatus.OPEN,
    )
}

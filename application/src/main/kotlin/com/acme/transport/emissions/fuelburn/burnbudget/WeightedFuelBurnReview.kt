package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel burn review is in its life cycle. */
enum class WeightedFuelBurnReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted fuel burn review as the fuel burn team keeps it. */
data class WeightedFuelBurnReview(
    var code: String,
    var name: String,
    var revision: Int,
    var limit: Int,
    var approved: Boolean,
    var status: WeightedFuelBurnReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnReviewStatus): Boolean {
        when (status) {
            WeightedFuelBurnReviewStatus.OPEN -> {
                when (next) {
                    WeightedFuelBurnReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedFuelBurnReviewStatus.IN_REVIEW -> {
                when (next) {
                    WeightedFuelBurnReviewStatus.RESOLVED, WeightedFuelBurnReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedFuelBurnReviewStatus.RESOLVED -> {
                when (next) {
                    WeightedFuelBurnReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedFuelBurnReviewStatus.REOPENED -> {
                when (next) {
                    WeightedFuelBurnReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnReviewStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn review $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round((revision.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): WeightedFuelBurnReview {
        return WeightedFuelBurnReview(
            code = code,
            name = name,
            revision = revision,
            limit = limit,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted fuel burn review in status open. */
fun newWeightedFuelBurnReview(code: String, name: String): WeightedFuelBurnReview {
    return WeightedFuelBurnReview(
        code = code,
        name = name,
        revision = 0,
        limit = 0,
        approved = false,
        status = WeightedFuelBurnReviewStatus.OPEN,
    )
}

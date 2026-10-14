package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn review is in its life cycle. */
enum class FuelBurnReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fuel burn review and its current status. */
data class FuelBurnReview(
    var code: String,
    var name: String,
    var windowDays: Int,
    var archived: Boolean,
    var unit: String,
    var budget: Double,
    var cost: Double,
    var driverId: String,
    var currency: String,
    var status: FuelBurnReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnReviewStatus): Boolean {
        when (status) {
            FuelBurnReviewStatus.CREATED -> {
                when (next) {
                    FuelBurnReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            FuelBurnReviewStatus.VALIDATED -> {
                when (next) {
                    FuelBurnReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            FuelBurnReviewStatus.PUBLISHED -> {
                when (next) {
                    FuelBurnReviewStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnReviewStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((cost + windowDays.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnReview {
        return FuelBurnReview(
            code = code,
            name = name,
            windowDays = windowDays,
            archived = archived,
            unit = unit,
            budget = budget,
            cost = cost,
            driverId = driverId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fuel burn review in status created. */
fun newFuelBurnReview(code: String, name: String): FuelBurnReview {
    return FuelBurnReview(
        code = code,
        name = name,
        windowDays = 0,
        archived = false,
        unit = "",
        budget = 0.0,
        cost = 0.0,
        driverId = "",
        currency = "",
        status = FuelBurnReviewStatus.CREATED,
    )
}

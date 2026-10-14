package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional fuel burn review records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a regional fuel burn review is in its life cycle. */
enum class RegionalFuelBurnReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional fuel burn review as the fuel burn team keeps it. */
data class RegionalFuelBurnReview(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var startsAt: Int,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var active: Boolean,
    var phone: String,
    var score: Double,
    var status: RegionalFuelBurnReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnReviewStatus): Boolean {
        when (status) {
            RegionalFuelBurnReviewStatus.CREATED -> return next == RegionalFuelBurnReviewStatus.RETIRED
            RegionalFuelBurnReviewStatus.VALIDATED -> {
                when (next) {
                    RegionalFuelBurnReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalFuelBurnReviewStatus.PUBLISHED -> return next == RegionalFuelBurnReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnReviewStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round(score * updatedAt.toDouble() * 100) / 100

    fun snapshot(): RegionalFuelBurnReview {
        return RegionalFuelBurnReview(
            code = code,
            title = title,
            updatedAt = updatedAt,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            active = active,
            phone = phone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional fuel burn review in status created. */
fun newRegionalFuelBurnReview(code: String, title: String): RegionalFuelBurnReview {
    return RegionalFuelBurnReview(
        code = code,
        title = title,
        updatedAt = 0,
        startsAt = 0,
        fuelLiters = 0.0,
        durationMinutes = 0,
        active = false,
        phone = "",
        score = 0.0,
        status = RegionalFuelBurnReviewStatus.CREATED,
    )
}

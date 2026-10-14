package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.normalize

/** Where a primary fuel burn review is in its life cycle. */
enum class PrimaryFuelBurnReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary fuel burn review as the fuel burn team keeps it. */
data class PrimaryFuelBurnReview(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var odometer: Int,
    var timezone: String,
    var status: PrimaryFuelBurnReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryFuelBurnReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryFuelBurnReviewStatus): Boolean {
        when (status) {
            PrimaryFuelBurnReviewStatus.IDLE -> return next == PrimaryFuelBurnReviewStatus.READY
            PrimaryFuelBurnReviewStatus.WARMING -> {
                when (next) {
                    PrimaryFuelBurnReviewStatus.DRAINING, PrimaryFuelBurnReviewStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryFuelBurnReviewStatus.READY -> {
                when (next) {
                    PrimaryFuelBurnReviewStatus.DRAINING, PrimaryFuelBurnReviewStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryFuelBurnReviewStatus.DRAINING -> return next == PrimaryFuelBurnReviewStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryFuelBurnReviewStatus) {
        if (!canMoveTo(next)) {
            error("primary fuel burn review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary fuel burn review"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): PrimaryFuelBurnReview {
        return PrimaryFuelBurnReview(
            code = code,
            title = title,
            tags = tags,
            odometer = odometer,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary fuel burn review in status idle. */
fun newPrimaryFuelBurnReview(code: String, title: String): PrimaryFuelBurnReview {
    return PrimaryFuelBurnReview(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        odometer = 0,
        timezone = "",
        status = PrimaryFuelBurnReviewStatus.IDLE,
    )
}

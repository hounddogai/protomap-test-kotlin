package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling lane plan review records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a rolling lane plan review is in its life cycle. */
enum class RollingLanePlanReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling lane plan review, with what the planners need to act on it. */
data class RollingLanePlanReview(
    var code: String,
    var title: String,
    var pallets: Int,
    var minimum: Double,
    var discount: Double,
    var driverId: String,
    var budget: Double,
    var trailerId: String,
    var reviewer: String,
    var fuelLiters: Double,
    var cost: Double,
    var status: RollingLanePlanReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLanePlanReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLanePlanReviewStatus): Boolean {
        when (status) {
            RollingLanePlanReviewStatus.PROPOSED -> {
                when (next) {
                    RollingLanePlanReviewStatus.APPROVED -> return true
                }
                return false
            }
            RollingLanePlanReviewStatus.APPROVED -> {
                when (next) {
                    RollingLanePlanReviewStatus.REJECTED, RollingLanePlanReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingLanePlanReviewStatus.REJECTED -> {
                when (next) {
                    RollingLanePlanReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingLanePlanReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling lane plan review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((minimum + pallets.toDouble()) * 100) / 100

    fun snapshot(): RollingLanePlanReview {
        return RollingLanePlanReview(
            code = code,
            title = title,
            pallets = pallets,
            minimum = minimum,
            discount = discount,
            driverId = driverId,
            budget = budget,
            trailerId = trailerId,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling lane plan review in status proposed. */
fun newRollingLanePlanReview(code: String, title: String): RollingLanePlanReview {
    return RollingLanePlanReview(
        code = code,
        title = title,
        pallets = 0,
        minimum = 0.0,
        discount = 0.0,
        driverId = "",
        budget = 0.0,
        trailerId = "",
        reviewer = "",
        fuelLiters = 0.0,
        cost = 0.0,
        status = RollingLanePlanReviewStatus.PROPOSED,
    )
}

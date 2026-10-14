package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered idling review records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a tiered idling review is in its life cycle. */
enum class TieredIdlingReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered idling review. Its code is unique within a tenant. */
data class TieredIdlingReview(
    var code: String,
    var name: String,
    var windowDays: Int,
    var etaMinutes: Int,
    var currency: String,
    var durationMinutes: Int,
    var ownerId: String,
    var amount: Double,
    var minimum: Double,
    var odometer: Int,
    var status: TieredIdlingReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIdlingReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIdlingReviewStatus): Boolean {
        when (status) {
            TieredIdlingReviewStatus.IDLE -> return next == TieredIdlingReviewStatus.WARMING
            TieredIdlingReviewStatus.WARMING -> return next == TieredIdlingReviewStatus.DRAINING
            TieredIdlingReviewStatus.READY -> return next == TieredIdlingReviewStatus.DRAINING || next == TieredIdlingReviewStatus.STOPPED
            TieredIdlingReviewStatus.DRAINING -> {
                when (next) {
                    TieredIdlingReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIdlingReviewStatus) {
        if (!canMoveTo(next)) {
            error("tiered idling review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round(etaMinutes.toDouble() * minimum * 100) / 100

    fun snapshot(): TieredIdlingReview {
        return TieredIdlingReview(
            code = code,
            name = name,
            windowDays = windowDays,
            etaMinutes = etaMinutes,
            currency = currency,
            durationMinutes = durationMinutes,
            ownerId = ownerId,
            amount = amount,
            minimum = minimum,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered idling review in status idle. */
fun newTieredIdlingReview(code: String, name: String): TieredIdlingReview {
    return TieredIdlingReview(
        code = code,
        name = name,
        windowDays = 0,
        etaMinutes = 0,
        currency = "",
        durationMinutes = 0,
        ownerId = "",
        amount = 0.0,
        minimum = 0.0,
        odometer = 0,
        status = TieredIdlingReviewStatus.IDLE,
    )
}

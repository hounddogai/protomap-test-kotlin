package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card estimate is in its life cycle. */
enum class RateCardEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rate card estimate, with what the planners need to act on it. */
data class RateCardEstimate(
    var code: String,
    var name: String,
    var quantity: Int,
    var maximum: Double,
    var expiresAt: Int,
    var segment: String,
    var priority: Int,
    var trailerId: String,
    var sequence: Int,
    var status: RateCardEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardEstimateStatus): Boolean {
        when (status) {
            RateCardEstimateStatus.CREATED -> return next == RateCardEstimateStatus.RETIRED
            RateCardEstimateStatus.VALIDATED -> return next == RateCardEstimateStatus.PUBLISHED || next == RateCardEstimateStatus.RETIRED
            RateCardEstimateStatus.PUBLISHED -> return next == RateCardEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RateCardEstimateStatus) {
        if (!canMoveTo(next)) {
            error("rate card estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rate card estimate"} $code"

    fun weightedTotal(): Double = round((maximum + sequence.toDouble()) * 100) / 100

    fun snapshot(): RateCardEstimate {
        return RateCardEstimate(
            code = code,
            name = name,
            quantity = quantity,
            maximum = maximum,
            expiresAt = expiresAt,
            segment = segment,
            priority = priority,
            trailerId = trailerId,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rate card estimate in status created. */
fun newRateCardEstimate(code: String, name: String): RateCardEstimate {
    return RateCardEstimate(
        code = code,
        name = name,
        quantity = 0,
        maximum = 0.0,
        expiresAt = 0,
        segment = "",
        priority = 0,
        trailerId = "",
        sequence = 0,
        status = RateCardEstimateStatus.CREATED,
    )
}

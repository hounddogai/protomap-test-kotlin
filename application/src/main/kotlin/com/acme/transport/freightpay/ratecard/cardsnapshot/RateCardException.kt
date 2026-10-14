package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card exception is in its life cycle. */
enum class RateCardExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rate card exception, with what the planners need to act on it. */
data class RateCardException(
    var code: String,
    var title: String,
    var attempts: Int,
    var region: String,
    var retries: Int,
    var category: String,
    var weight: Double,
    var trailerId: String,
    var status: RateCardExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardExceptionStatus): Boolean {
        when (status) {
            RateCardExceptionStatus.OPEN -> return next == RateCardExceptionStatus.RESOLVED
            RateCardExceptionStatus.IN_REVIEW -> {
                when (next) {
                    RateCardExceptionStatus.RESOLVED, RateCardExceptionStatus.REOPENED -> return true
                }
                return false
            }
            RateCardExceptionStatus.RESOLVED -> {
                when (next) {
                    RateCardExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            RateCardExceptionStatus.REOPENED -> {
                when (next) {
                    RateCardExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardExceptionStatus) {
        if (!canMoveTo(next)) {
            error("rate card exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((retries.toDouble() - weight) * 100) / 100

    fun snapshot(): RateCardException {
        return RateCardException(
            code = code,
            title = title,
            attempts = attempts,
            region = region,
            retries = retries,
            category = category,
            weight = weight,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rate card exception in status open. */
fun newRateCardException(code: String, title: String): RateCardException {
    return RateCardException(
        code = code,
        title = title,
        attempts = 0,
        region = "",
        retries = 0,
        category = "",
        weight = 0.0,
        trailerId = "",
        status = RateCardExceptionStatus.OPEN,
    )
}

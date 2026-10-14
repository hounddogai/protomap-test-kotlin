package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card summary is in its life cycle. */
enum class RateCardSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rate card summary as the rate card team keeps it. */
data class RateCardSummary(
    var code: String,
    var name: String,
    var summary: String,
    var attempts: Int,
    var locale: String,
    var odometer: Int,
    var status: RateCardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardSummaryStatus): Boolean {
        when (status) {
            RateCardSummaryStatus.CREATED -> return next == RateCardSummaryStatus.PUBLISHED || next == RateCardSummaryStatus.RETIRED
            RateCardSummaryStatus.VALIDATED -> return next == RateCardSummaryStatus.PUBLISHED || next == RateCardSummaryStatus.RETIRED
            RateCardSummaryStatus.PUBLISHED -> return next == RateCardSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RateCardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rate card summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((attempts.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): RateCardSummary {
        return RateCardSummary(
            code = code,
            name = name,
            summary = summary,
            attempts = attempts,
            locale = locale,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rate card summary in status created. */
fun newRateCardSummary(code: String, name: String): RateCardSummary {
    return RateCardSummary(
        code = code,
        name = name,
        summary = "",
        attempts = 0,
        locale = "",
        odometer = 0,
        status = RateCardSummaryStatus.CREATED,
    )
}

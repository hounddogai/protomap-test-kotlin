package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy rate card summary records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a legacy rate card summary is in its life cycle. */
enum class LegacyRateCardSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a legacy rate card summary and its current status. */
data class LegacyRateCardSummary(
    var code: String,
    var name: String,
    var rate: Double,
    var expiresAt: Int,
    var pallets: Int,
    var score: Double,
    var reviewer: String,
    var endsAt: Int,
    var threshold: Double,
    var priority: Int,
    var status: LegacyRateCardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRateCardSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRateCardSummaryStatus): Boolean {
        when (status) {
            LegacyRateCardSummaryStatus.CREATED -> {
                when (next) {
                    LegacyRateCardSummaryStatus.VALIDATED -> return true
                }
                return false
            }
            LegacyRateCardSummaryStatus.VALIDATED -> {
                when (next) {
                    LegacyRateCardSummaryStatus.PUBLISHED, LegacyRateCardSummaryStatus.RETIRED -> return true
                }
                return false
            }
            LegacyRateCardSummaryStatus.PUBLISHED -> return next == LegacyRateCardSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyRateCardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("legacy rate card summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round((expiresAt.toDouble() - rate) * 100) / 100

    fun snapshot(): LegacyRateCardSummary {
        return LegacyRateCardSummary(
            code = code,
            name = name,
            rate = rate,
            expiresAt = expiresAt,
            pallets = pallets,
            score = score,
            reviewer = reviewer,
            endsAt = endsAt,
            threshold = threshold,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy rate card summary in status created. */
fun newLegacyRateCardSummary(code: String, name: String): LegacyRateCardSummary {
    return LegacyRateCardSummary(
        code = code,
        name = name,
        rate = 0.0,
        expiresAt = 0,
        pallets = 0,
        score = 0.0,
        reviewer = "",
        endsAt = 0,
        threshold = 0.0,
        priority = 0,
        status = LegacyRateCardSummaryStatus.CREATED,
    )
}

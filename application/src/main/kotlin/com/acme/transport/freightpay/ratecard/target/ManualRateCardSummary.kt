package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual rate card summary is in its life cycle. */
enum class ManualRateCardSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual rate card summary. Its code is unique within a tenant. */
data class ManualRateCardSummary(
    var code: String,
    var label: String,
    var depot: String,
    var rate: Double,
    var budget: Double,
    var status: ManualRateCardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRateCardSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRateCardSummaryStatus): Boolean {
        when (status) {
            ManualRateCardSummaryStatus.REQUESTED -> return next == ManualRateCardSummaryStatus.EXPIRED
            ManualRateCardSummaryStatus.GRANTED -> return next == ManualRateCardSummaryStatus.REVOKED
            ManualRateCardSummaryStatus.REVOKED -> {
                when (next) {
                    ManualRateCardSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRateCardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual rate card summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round((budget + rate) * 100) / 100

    fun snapshot(): ManualRateCardSummary {
        return ManualRateCardSummary(
            code = code,
            label = label,
            depot = depot,
            rate = rate,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual rate card summary in status requested. */
fun newManualRateCardSummary(code: String, label: String): ManualRateCardSummary {
    return ManualRateCardSummary(
        code = code,
        label = label,
        depot = "",
        rate = 0.0,
        budget = 0.0,
        status = ManualRateCardSummaryStatus.REQUESTED,
    )
}

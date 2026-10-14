package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling rate card summary is in its life cycle. */
enum class RollingRateCardSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling rate card summary. Its code is unique within a tenant. */
data class RollingRateCardSummary(
    var code: String,
    var label: String,
    var amount: Double,
    var stopCount: Int,
    var ratio: Double,
    var pinned: Boolean,
    var budget: Double,
    var status: RollingRateCardSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRateCardSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRateCardSummaryStatus): Boolean {
        when (status) {
            RollingRateCardSummaryStatus.PLANNED -> return next == RollingRateCardSummaryStatus.SCHEDULED || next == RollingRateCardSummaryStatus.COMPLETED
            RollingRateCardSummaryStatus.SCHEDULED -> return next == RollingRateCardSummaryStatus.IN_PROGRESS
            RollingRateCardSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    RollingRateCardSummaryStatus.COMPLETED, RollingRateCardSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RollingRateCardSummaryStatus.COMPLETED -> return next == RollingRateCardSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingRateCardSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rolling rate card summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling rate card summary"} $code"

    fun effectiveExposure(): Double = round((budget - amount) * 100) / 100

    fun snapshot(): RollingRateCardSummary {
        return RollingRateCardSummary(
            code = code,
            label = label,
            amount = amount,
            stopCount = stopCount,
            ratio = ratio,
            pinned = pinned,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling rate card summary in status planned. */
fun newRollingRateCardSummary(code: String, label: String): RollingRateCardSummary {
    return RollingRateCardSummary(
        code = code,
        label = label,
        amount = 0.0,
        stopCount = 0,
        ratio = 0.0,
        pinned = false,
        budget = 0.0,
        status = RollingRateCardSummaryStatus.PLANNED,
    )
}

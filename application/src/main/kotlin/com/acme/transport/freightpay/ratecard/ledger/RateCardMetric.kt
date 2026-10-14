package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card metric is in its life cycle. */
enum class RateCardMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rate card metric and its current status. */
data class RateCardMetric(
    var code: String,
    var title: String,
    var budget: Double,
    var contactEmail: String,
    var phone: String,
    var etaMinutes: Int,
    var status: RateCardMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardMetricStatus): Boolean {
        when (status) {
            RateCardMetricStatus.DRAFT -> return next == RateCardMetricStatus.ACTIVE
            RateCardMetricStatus.PENDING -> return next == RateCardMetricStatus.ACTIVE
            RateCardMetricStatus.ACTIVE -> {
                when (next) {
                    RateCardMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            RateCardMetricStatus.SUSPENDED -> {
                when (next) {
                    RateCardMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardMetricStatus) {
        if (!canMoveTo(next)) {
            error("rate card metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round(etaMinutes.toDouble() * budget * 100) / 100

    fun snapshot(): RateCardMetric {
        return RateCardMetric(
            code = code,
            title = title,
            budget = budget,
            contactEmail = contactEmail,
            phone = phone,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card metric in status draft. */
fun newRateCardMetric(code: String, title: String): RateCardMetric {
    return RateCardMetric(
        code = code,
        title = title,
        budget = 0.0,
        contactEmail = "",
        phone = "",
        etaMinutes = 0,
        status = RateCardMetricStatus.DRAFT,
    )
}

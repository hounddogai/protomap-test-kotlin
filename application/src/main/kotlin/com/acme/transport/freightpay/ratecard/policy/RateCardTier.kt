package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card tier is in its life cycle. */
enum class RateCardTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rate card tier as the rate card team keeps it. */
data class RateCardTier(
    var code: String,
    var title: String,
    var label: String,
    var currency: String,
    var phone: String,
    var channel: String,
    var locale: String,
    var ratio: Double,
    var maximum: Double,
    var status: RateCardTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardTierStatus): Boolean {
        when (status) {
            RateCardTierStatus.PLANNED -> {
                when (next) {
                    RateCardTierStatus.IN_PROGRESS, RateCardTierStatus.COMPLETED -> return true
                }
                return false
            }
            RateCardTierStatus.SCHEDULED -> return next == RateCardTierStatus.CANCELLED
            RateCardTierStatus.IN_PROGRESS -> return next == RateCardTierStatus.CANCELLED
            RateCardTierStatus.COMPLETED -> return next == RateCardTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RateCardTierStatus) {
        if (!canMoveTo(next)) {
            error("rate card tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rate card tier"} $code"

    fun netExposure(): Double = round((maximum - ratio) * 100) / 100

    fun snapshot(): RateCardTier {
        return RateCardTier(
            code = code,
            title = title,
            label = label,
            currency = currency,
            phone = phone,
            channel = channel,
            locale = locale,
            ratio = ratio,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rate card tier in status planned. */
fun newRateCardTier(code: String, title: String): RateCardTier {
    return RateCardTier(
        code = code,
        title = title,
        label = "",
        currency = "",
        phone = "",
        channel = "",
        locale = "",
        ratio = 0.0,
        maximum = 0.0,
        status = RateCardTierStatus.PLANNED,
    )
}

package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card variance is in its life cycle. */
enum class RateCardVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rate card variance. Its code is unique within a tenant. */
data class RateCardVariance(
    var code: String,
    var name: String,
    var weight: Double,
    var score: Double,
    var etaMinutes: Int,
    var title: String,
    var attempts: Int,
    var amount: Double,
    var country: String,
    var status: RateCardVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardVarianceStatus): Boolean {
        when (status) {
            RateCardVarianceStatus.NEW -> return next == RateCardVarianceStatus.DONE
            RateCardVarianceStatus.QUEUED -> {
                when (next) {
                    RateCardVarianceStatus.DONE, RateCardVarianceStatus.FAILED -> return true
                }
                return false
            }
            RateCardVarianceStatus.RUNNING -> {
                when (next) {
                    RateCardVarianceStatus.DONE -> return true
                }
                return false
            }
            RateCardVarianceStatus.DONE -> return next == RateCardVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RateCardVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rate card variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rate card variance"} $code"

    fun effectiveValue(): Double = round((amount - weight) * 100) / 100

    fun snapshot(): RateCardVariance {
        return RateCardVariance(
            code = code,
            name = name,
            weight = weight,
            score = score,
            etaMinutes = etaMinutes,
            title = title,
            attempts = attempts,
            amount = amount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rate card variance in status new. */
fun newRateCardVariance(code: String, name: String): RateCardVariance {
    return RateCardVariance(
        code = code,
        name = name,
        weight = 0.0,
        score = 0.0,
        etaMinutes = 0,
        title = "",
        attempts = 0,
        amount = 0.0,
        country = "",
        status = RateCardVarianceStatus.NEW,
    )
}

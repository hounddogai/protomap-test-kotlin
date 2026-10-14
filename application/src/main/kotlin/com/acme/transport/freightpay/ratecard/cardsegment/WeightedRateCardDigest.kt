package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rate card digest is in its life cycle. */
enum class WeightedRateCardDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted rate card digest as the rate card team keeps it. */
data class WeightedRateCardDigest(
    var code: String,
    var title: String,
    var graceDays: Int,
    var etaMinutes: Int,
    var limit: Int,
    var maximum: Double,
    var createdAt: Int,
    var odometer: Int,
    var name: String,
    var cost: Double,
    var status: WeightedRateCardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRateCardDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRateCardDigestStatus): Boolean {
        when (status) {
            WeightedRateCardDigestStatus.NEW -> return next == WeightedRateCardDigestStatus.DONE || next == WeightedRateCardDigestStatus.FAILED
            WeightedRateCardDigestStatus.QUEUED -> {
                when (next) {
                    WeightedRateCardDigestStatus.FAILED -> return true
                }
                return false
            }
            WeightedRateCardDigestStatus.RUNNING -> {
                when (next) {
                    WeightedRateCardDigestStatus.FAILED -> return true
                }
                return false
            }
            WeightedRateCardDigestStatus.DONE -> {
                when (next) {
                    WeightedRateCardDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRateCardDigestStatus) {
        if (!canMoveTo(next)) {
            error("weighted rate card digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((etaMinutes.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): WeightedRateCardDigest {
        return WeightedRateCardDigest(
            code = code,
            title = title,
            graceDays = graceDays,
            etaMinutes = etaMinutes,
            limit = limit,
            maximum = maximum,
            createdAt = createdAt,
            odometer = odometer,
            name = name,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted rate card digest in status new. */
fun newWeightedRateCardDigest(code: String, title: String): WeightedRateCardDigest {
    return WeightedRateCardDigest(
        code = code,
        title = title,
        graceDays = 0,
        etaMinutes = 0,
        limit = 0,
        maximum = 0.0,
        createdAt = 0,
        odometer = 0,
        name = "",
        cost = 0.0,
        status = WeightedRateCardDigestStatus.NEW,
    )
}

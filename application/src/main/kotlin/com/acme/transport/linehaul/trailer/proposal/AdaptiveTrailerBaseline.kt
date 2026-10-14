package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive trailer baseline is in its life cycle. */
enum class AdaptiveTrailerBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive trailer baseline and its current status. */
data class AdaptiveTrailerBaseline(
    var code: String,
    var name: String,
    var currency: String,
    var discount: Double,
    var score: Double,
    var odometer: Int,
    var capacity: Int,
    var status: AdaptiveTrailerBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerBaselineStatus): Boolean {
        when (status) {
            AdaptiveTrailerBaselineStatus.PLANNED -> return next == AdaptiveTrailerBaselineStatus.SCHEDULED || next == AdaptiveTrailerBaselineStatus.CANCELLED
            AdaptiveTrailerBaselineStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveTrailerBaselineStatus.IN_PROGRESS, AdaptiveTrailerBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveTrailerBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveTrailerBaselineStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveTrailerBaselineStatus.COMPLETED -> return next == AdaptiveTrailerBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerBaselineStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((discount + score) * 100) / 100

    fun snapshot(): AdaptiveTrailerBaseline {
        return AdaptiveTrailerBaseline(
            code = code,
            name = name,
            currency = currency,
            discount = discount,
            score = score,
            odometer = odometer,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive trailer baseline in status planned. */
fun newAdaptiveTrailerBaseline(code: String, name: String): AdaptiveTrailerBaseline {
    return AdaptiveTrailerBaseline(
        code = code,
        name = name,
        currency = "",
        discount = 0.0,
        score = 0.0,
        odometer = 0,
        capacity = 0,
        status = AdaptiveTrailerBaselineStatus.PLANNED,
    )
}

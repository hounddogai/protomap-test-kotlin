package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rate card threshold is in its life cycle. */
enum class WeightedRateCardThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted rate card threshold as the rate card team keeps it. */
data class WeightedRateCardThreshold(
    var code: String,
    var name: String,
    var odometer: Int,
    var createdAt: Int,
    var version: Int,
    var externalRef: String,
    var status: WeightedRateCardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRateCardThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRateCardThresholdStatus): Boolean {
        when (status) {
            WeightedRateCardThresholdStatus.PROPOSED -> {
                when (next) {
                    WeightedRateCardThresholdStatus.APPROVED -> return true
                }
                return false
            }
            WeightedRateCardThresholdStatus.APPROVED -> {
                when (next) {
                    WeightedRateCardThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedRateCardThresholdStatus.REJECTED -> return next == WeightedRateCardThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedRateCardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("weighted rate card threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"weighted rate card threshold"} $code"

    fun adjustedTotal(): Double = round(version.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): WeightedRateCardThreshold {
        return WeightedRateCardThreshold(
            code = code,
            name = name,
            odometer = odometer,
            createdAt = createdAt,
            version = version,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted rate card threshold in status proposed. */
fun newWeightedRateCardThreshold(code: String, name: String): WeightedRateCardThreshold {
    return WeightedRateCardThreshold(
        code = code,
        name = name,
        odometer = 0,
        createdAt = 0,
        version = 0,
        externalRef = "",
        status = WeightedRateCardThresholdStatus.PROPOSED,
    )
}

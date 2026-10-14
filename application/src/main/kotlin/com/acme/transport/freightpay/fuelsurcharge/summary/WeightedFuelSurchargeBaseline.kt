package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel surcharge baseline is in its life cycle. */
enum class WeightedFuelSurchargeBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One weighted fuel surcharge baseline, with what the planners need to act on it. */
data class WeightedFuelSurchargeBaseline(
    var code: String,
    var name: String,
    var depot: String,
    var durationMinutes: Int,
    var stopCount: Int,
    var endsAt: Int,
    var expiresAt: Int,
    var version: Int,
    var status: WeightedFuelSurchargeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelSurchargeBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelSurchargeBaselineStatus): Boolean {
        when (status) {
            WeightedFuelSurchargeBaselineStatus.PROPOSED -> {
                when (next) {
                    WeightedFuelSurchargeBaselineStatus.REJECTED -> return true
                }
                return false
            }
            WeightedFuelSurchargeBaselineStatus.APPROVED -> {
                when (next) {
                    WeightedFuelSurchargeBaselineStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedFuelSurchargeBaselineStatus.REJECTED -> return next == WeightedFuelSurchargeBaselineStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedFuelSurchargeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel surcharge baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((endsAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedFuelSurchargeBaseline {
        return WeightedFuelSurchargeBaseline(
            code = code,
            name = name,
            depot = depot,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            endsAt = endsAt,
            expiresAt = expiresAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted fuel surcharge baseline in status proposed. */
fun newWeightedFuelSurchargeBaseline(code: String, name: String): WeightedFuelSurchargeBaseline {
    return WeightedFuelSurchargeBaseline(
        code = code,
        name = name,
        depot = "",
        durationMinutes = 0,
        stopCount = 0,
        endsAt = 0,
        expiresAt = 0,
        version = 0,
        status = WeightedFuelSurchargeBaselineStatus.PROPOSED,
    )
}

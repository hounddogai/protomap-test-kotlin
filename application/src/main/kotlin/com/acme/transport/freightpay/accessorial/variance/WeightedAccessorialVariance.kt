package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted accessorial variance records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a weighted accessorial variance is in its life cycle. */
enum class WeightedAccessorialVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One weighted accessorial variance, with what the planners need to act on it. */
data class WeightedAccessorialVariance(
    var code: String,
    var title: String,
    var routeCode: String,
    var source: String,
    var expiresAt: Int,
    var unit: String,
    var pallets: Int,
    var amount: Double,
    var channel: String,
    var durationMinutes: Int,
    var quantity: Int,
    var status: WeightedAccessorialVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAccessorialVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAccessorialVarianceStatus): Boolean {
        when (status) {
            WeightedAccessorialVarianceStatus.PLANNED -> return next == WeightedAccessorialVarianceStatus.SCHEDULED || next == WeightedAccessorialVarianceStatus.CANCELLED
            WeightedAccessorialVarianceStatus.SCHEDULED -> return next == WeightedAccessorialVarianceStatus.IN_PROGRESS || next == WeightedAccessorialVarianceStatus.CANCELLED
            WeightedAccessorialVarianceStatus.IN_PROGRESS -> return next == WeightedAccessorialVarianceStatus.COMPLETED || next == WeightedAccessorialVarianceStatus.CANCELLED
            WeightedAccessorialVarianceStatus.COMPLETED -> {
                when (next) {
                    WeightedAccessorialVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAccessorialVarianceStatus) {
        if (!canMoveTo(next)) {
            error("weighted accessorial variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveValue(): Double = round((quantity.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): WeightedAccessorialVariance {
        return WeightedAccessorialVariance(
            code = code,
            title = title,
            routeCode = routeCode,
            source = source,
            expiresAt = expiresAt,
            unit = unit,
            pallets = pallets,
            amount = amount,
            channel = channel,
            durationMinutes = durationMinutes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted accessorial variance in status planned. */
fun newWeightedAccessorialVariance(code: String, title: String): WeightedAccessorialVariance {
    return WeightedAccessorialVariance(
        code = code,
        title = title,
        routeCode = "",
        source = "",
        expiresAt = 0,
        unit = "",
        pallets = 0,
        amount = 0.0,
        channel = "",
        durationMinutes = 0,
        quantity = 0,
        status = WeightedAccessorialVarianceStatus.PLANNED,
    )
}

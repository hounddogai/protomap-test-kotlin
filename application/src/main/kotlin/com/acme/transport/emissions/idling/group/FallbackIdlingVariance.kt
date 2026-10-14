package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback idling variance is in its life cycle. */
enum class FallbackIdlingVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback idling variance, with what the planners need to act on it. */
data class FallbackIdlingVariance(
    var code: String,
    var label: String,
    var amount: Double,
    var approved: Boolean,
    var locale: String,
    var ownerId: String,
    var country: String,
    var version: Int,
    var status: FallbackIdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIdlingVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIdlingVarianceStatus): Boolean {
        when (status) {
            FallbackIdlingVarianceStatus.PROPOSED -> return next == FallbackIdlingVarianceStatus.APPROVED || next == FallbackIdlingVarianceStatus.REJECTED
            FallbackIdlingVarianceStatus.APPROVED -> {
                when (next) {
                    FallbackIdlingVarianceStatus.REJECTED, FallbackIdlingVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackIdlingVarianceStatus.REJECTED -> {
                when (next) {
                    FallbackIdlingVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackIdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback idling variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"fallback idling variance"} $code"

    fun projectedVolume(): Double = round(version.toDouble() * amount * 100) / 100

    fun snapshot(): FallbackIdlingVariance {
        return FallbackIdlingVariance(
            code = code,
            label = label,
            amount = amount,
            approved = approved,
            locale = locale,
            ownerId = ownerId,
            country = country,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback idling variance in status proposed. */
fun newFallbackIdlingVariance(code: String, label: String): FallbackIdlingVariance {
    return FallbackIdlingVariance(
        code = code,
        label = label,
        amount = 0.0,
        approved = false,
        locale = "",
        ownerId = "",
        country = "",
        version = 0,
        status = FallbackIdlingVarianceStatus.PROPOSED,
    )
}

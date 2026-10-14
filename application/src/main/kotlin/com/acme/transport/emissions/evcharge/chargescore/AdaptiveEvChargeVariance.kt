package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.text.normalize

/** Where a adaptive ev charge variance is in its life cycle. */
enum class AdaptiveEvChargeVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a adaptive ev charge variance and its current status. */
data class AdaptiveEvChargeVariance(
    var code: String,
    var name: String,
    var checksum: String,
    var timezone: String,
    var limit: Int,
    var status: AdaptiveEvChargeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEvChargeVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEvChargeVarianceStatus): Boolean {
        when (status) {
            AdaptiveEvChargeVarianceStatus.PROPOSED -> return next == AdaptiveEvChargeVarianceStatus.APPROVED || next == AdaptiveEvChargeVarianceStatus.WITHDRAWN
            AdaptiveEvChargeVarianceStatus.APPROVED -> {
                when (next) {
                    AdaptiveEvChargeVarianceStatus.REJECTED, AdaptiveEvChargeVarianceStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveEvChargeVarianceStatus.REJECTED -> return next == AdaptiveEvChargeVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveEvChargeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive ev charge variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): AdaptiveEvChargeVariance {
        return AdaptiveEvChargeVariance(
            code = code,
            name = name,
            checksum = checksum,
            timezone = timezone,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive ev charge variance in status proposed. */
fun newAdaptiveEvChargeVariance(code: String, name: String): AdaptiveEvChargeVariance {
    return AdaptiveEvChargeVariance(
        code = code,
        name = name,
        checksum = "",
        timezone = "",
        limit = 0,
        status = AdaptiveEvChargeVarianceStatus.PROPOSED,
    )
}

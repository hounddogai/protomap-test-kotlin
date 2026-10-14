package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling cutoff variance is in its life cycle. */
enum class RollingCutoffVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling cutoff variance as the cutoff team keeps it. */
data class RollingCutoffVariance(
    var code: String,
    var name: String,
    var startsAt: Int,
    var currency: String,
    var description: String,
    var routeCode: String,
    var threshold: Double,
    var status: RollingCutoffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCutoffVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCutoffVarianceStatus): Boolean {
        when (status) {
            RollingCutoffVarianceStatus.CREATED -> {
                when (next) {
                    RollingCutoffVarianceStatus.VALIDATED, RollingCutoffVarianceStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingCutoffVarianceStatus.VALIDATED -> return next == RollingCutoffVarianceStatus.PUBLISHED
            RollingCutoffVarianceStatus.PUBLISHED -> return next == RollingCutoffVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingCutoffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rolling cutoff variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round(startsAt.toDouble() * threshold * 100) / 100

    fun snapshot(): RollingCutoffVariance {
        return RollingCutoffVariance(
            code = code,
            name = name,
            startsAt = startsAt,
            currency = currency,
            description = description,
            routeCode = routeCode,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling cutoff variance in status created. */
fun newRollingCutoffVariance(code: String, name: String): RollingCutoffVariance {
    return RollingCutoffVariance(
        code = code,
        name = name,
        startsAt = 0,
        currency = "",
        description = "",
        routeCode = "",
        threshold = 0.0,
        status = RollingCutoffVarianceStatus.CREATED,
    )
}

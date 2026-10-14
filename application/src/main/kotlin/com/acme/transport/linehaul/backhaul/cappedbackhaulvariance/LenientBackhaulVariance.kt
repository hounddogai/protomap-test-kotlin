package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient backhaul variance records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a lenient backhaul variance is in its life cycle. */
enum class LenientBackhaulVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient backhaul variance, with what the planners need to act on it. */
data class LenientBackhaulVariance(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var trailerId: String,
    var cost: Double,
    var amount: Double,
    var country: String,
    var status: LenientBackhaulVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientBackhaulVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientBackhaulVarianceStatus): Boolean {
        when (status) {
            LenientBackhaulVarianceStatus.CREATED -> {
                when (next) {
                    LenientBackhaulVarianceStatus.VALIDATED -> return true
                }
                return false
            }
            LenientBackhaulVarianceStatus.VALIDATED -> return next == LenientBackhaulVarianceStatus.PUBLISHED
            LenientBackhaulVarianceStatus.PUBLISHED -> return next == LenientBackhaulVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientBackhaulVarianceStatus) {
        if (!canMoveTo(next)) {
            error("lenient backhaul variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((amount + cost) * 100) / 100

    fun snapshot(): LenientBackhaulVariance {
        return LenientBackhaulVariance(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            tags = tags,
            trailerId = trailerId,
            cost = cost,
            amount = amount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient backhaul variance in status created. */
fun newLenientBackhaulVariance(code: String, label: String): LenientBackhaulVariance {
    return LenientBackhaulVariance(
        code = code,
        label = label,
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        trailerId = "",
        cost = 0.0,
        amount = 0.0,
        country = "",
        status = LenientBackhaulVarianceStatus.CREATED,
    )
}

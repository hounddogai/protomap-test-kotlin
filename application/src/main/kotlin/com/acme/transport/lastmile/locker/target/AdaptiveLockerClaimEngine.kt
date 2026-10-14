package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive locker claim calculation. */
interface AdaptiveLockerClaimAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveLockerClaim, value: Double): Double
}

data class CapAdaptiveLockerClaimAdjuster(var cap: Double) : AdaptiveLockerClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLockerClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveLockerClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLockerClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLockerClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotAdaptiveLockerClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLockerClaimAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: AdaptiveLockerClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveLockerClaimAdjuster(var step: Double) : AdaptiveLockerClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLockerClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveLockerClaimAdjuster(var minimum: Double) : AdaptiveLockerClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLockerClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveLockerClaimAdjuster(var percent: Double) : AdaptiveLockerClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLockerClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive locker claim steps in order and keeps a trace of each. */
data class AdaptiveLockerClaimEngine(
    var steps: MutableList<AdaptiveLockerClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveLockerClaim): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLockerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveLockerClaimEngine(flags: Registry): AdaptiveLockerClaimEngine {
    return AdaptiveLockerClaimEngine(
        steps = mutableListOf<AdaptiveLockerClaimAdjuster>(
            CapAdaptiveLockerClaimAdjuster(26317.0),
            FlaggedAdaptiveLockerClaimAdjuster(flags, "adaptive-locker-claim-weighted", 7.2),
            DepotAdaptiveLockerClaimAdjuster(
                mutableMapOf<String, Double>("online" to 0.79, "east" to 1.11, "retail" to 0.98),
            ),
            RoundingAdaptiveLockerClaimAdjuster(0.5),
            MinimumAdaptiveLockerClaimAdjuster(2.8),
            PercentAdaptiveLockerClaimAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 43895.3,
    )
}

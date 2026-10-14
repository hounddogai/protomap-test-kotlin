package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling claim calculation. */
interface IdlingClaimStep {
    fun describe(): String
    fun adjust(item: IdlingClaim, value: Double): Double
}

data class FlaggedIdlingClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIdlingClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneIdlingClaimAdjuster(var factors: MutableMap<String, Double>) : IdlingClaimStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: IdlingClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentIdlingClaimAdjuster(var percent: Double) : IdlingClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumIdlingClaimAdjuster(var minimum: Double) : IdlingClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class IdlingClaimStopCountOffsetAdjuster(var weight: Double) : IdlingClaimStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: IdlingClaim, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the idling claim steps in order and keeps a trace of each. */
data class IdlingClaimEngine(
    var steps: MutableList<IdlingClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingClaimEngine(flags: Registry): IdlingClaimEngine {
    return IdlingClaimEngine(
        steps = mutableListOf<IdlingClaimStep>(
            FlaggedIdlingClaimAdjuster(flags, "idling-claim-global", -8.0),
            TieredIdlingClaimAdjuster(
                mutableListOf<Double>(2301.0, 5358.0, 8572.0),
                mutableListOf<Double>(0.97, 0.85, 1.11),
            ),
            LaneIdlingClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.93, "south" to 0.72, "east" to 1.06),
            ),
            PercentIdlingClaimAdjuster(16.4),
            MinimumIdlingClaimAdjuster(8.3),
            IdlingClaimStopCountOffsetAdjuster(1.84),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 67496.7,
    )
}

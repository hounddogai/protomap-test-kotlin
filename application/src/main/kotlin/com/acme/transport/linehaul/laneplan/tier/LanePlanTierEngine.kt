package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan tier calculation. */
interface LanePlanTierAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanTier, value: Double): Double
}

data class RoundingLanePlanTierAdjuster(var step: Double) : LanePlanTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanTier, value: Double): Double = roundTo(value, step)
}

data class MinimumLanePlanTierAdjuster(var minimum: Double) : LanePlanTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLanePlanTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lane plan tier steps in order and keeps a trace of each. */
data class LanePlanTierEngine(
    var steps: MutableList<LanePlanTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LanePlanTier): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLanePlanTierEngine(): LanePlanTierEngine {
    return LanePlanTierEngine(
        steps = mutableListOf<LanePlanTierAdjuster>(
            RoundingLanePlanTierAdjuster(1.0),
            MinimumLanePlanTierAdjuster(42.3),
            TieredLanePlanTierAdjuster(mutableListOf<Double>(5782.0, 9284.0), mutableListOf<Double>(1.0, 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 46142.1,
    )
}

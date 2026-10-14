package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted roster tier calculation. */
interface WeightedRosterTierFactor {
    fun describe(): String
    fun adjust(item: WeightedRosterTier, value: Double): Double
}

data class FlaggedWeightedRosterTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedRosterTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedRosterTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleWeightedRosterTierAdjuster(var factors: MutableMap<String, Double>) : WeightedRosterTierFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedRosterTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedRosterTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRosterTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRosterTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedRosterTierAdjuster(var percent: Double) : WeightedRosterTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRosterTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedRosterTierAdjuster(var minimum: Double) : WeightedRosterTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRosterTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedRosterTierAdjuster(var step: Double) : WeightedRosterTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRosterTier, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted roster tier steps in order and keeps a trace of each. */
data class WeightedRosterTierEngine(
    var steps: MutableList<WeightedRosterTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedRosterTier): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRosterTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedRosterTierEngine(flags: Registry): WeightedRosterTierEngine {
    return WeightedRosterTierEngine(
        steps = mutableListOf<WeightedRosterTierFactor>(
            FlaggedWeightedRosterTierAdjuster(flags, "weighted-roster-tier-pending", -3.2),
            TitleWeightedRosterTierAdjuster(mutableMapOf<String, Double>("east" to 0.75)),
            TieredWeightedRosterTierAdjuster(
                mutableListOf<Double>(327.0, 1737.0, 5987.0, 9022.0),
                mutableListOf<Double>(0.9, 0.87, 0.99, 0.9),
            ),
            PercentWeightedRosterTierAdjuster(-13.6),
            MinimumWeightedRosterTierAdjuster(8.8),
            RoundingWeightedRosterTierAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 2464.4,
    )
}

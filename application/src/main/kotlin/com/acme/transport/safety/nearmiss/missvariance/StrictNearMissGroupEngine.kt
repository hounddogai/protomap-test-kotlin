package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict near miss group calculation. */
interface StrictNearMissGroupStep {
    fun describe(): String
    fun adjust(item: StrictNearMissGroup, value: Double): Double
}

data class FlaggedStrictNearMissGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictNearMissGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictNearMissGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictNearMissGroupAdjuster(var step: Double) : StrictNearMissGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictNearMissGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumStrictNearMissGroupAdjuster(var minimum: Double) : StrictNearMissGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictNearMissGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictNearMissGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictNearMissGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictNearMissGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictNearMissGroupOdometerOffsetAdjuster(var weight: Double) : StrictNearMissGroupStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: StrictNearMissGroup, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class RegionStrictNearMissGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictNearMissGroupStep {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictNearMissGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict near miss group steps in order and keeps a trace of each. */
data class StrictNearMissGroupEngine(
    var steps: MutableList<StrictNearMissGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictNearMissGroup): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictNearMissGroupStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictNearMissGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictNearMissGroupEngine(flags: Registry): StrictNearMissGroupEngine {
    return StrictNearMissGroupEngine(
        steps = mutableListOf<StrictNearMissGroupStep>(
            FlaggedStrictNearMissGroupAdjuster(flags, "strict-near-miss-group-default", 1.8),
            RoundingStrictNearMissGroupAdjuster(0.1),
            MinimumStrictNearMissGroupAdjuster(28.0),
            TieredStrictNearMissGroupAdjuster(
                mutableListOf<Double>(407.0, 1710.0, 8284.0),
                mutableListOf<Double>(0.97, 0.86, 0.84),
            ),
            StrictNearMissGroupOdometerOffsetAdjuster(0.4),
            RegionStrictNearMissGroupAdjuster(mutableMapOf<String, Double>("online" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 55989.9,
    )
}

package com.acme.transport.safety.coaching.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the weighted coaching bundle calculation. */
interface WeightedCoachingBundleAdjuster {
    fun describe(): String
    fun adjust(item: WeightedCoachingBundle, value: Double): Double
}

data class FlaggedWeightedCoachingBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCoachingBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCoachingBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedCoachingBundleCeilingOffsetAdjuster(var weight: Double) : WeightedCoachingBundleAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: WeightedCoachingBundle, value: Double): Double = value + item.ceiling * weight
}

data class TieredWeightedCoachingBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCoachingBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCoachingBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted coaching bundle steps in order and keeps a trace of each. */
data class WeightedCoachingBundleEngine(
    var steps: MutableList<WeightedCoachingBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedCoachingBundle): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCoachingBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCoachingBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedCoachingBundleEngine(flags: Registry): WeightedCoachingBundleEngine {
    return WeightedCoachingBundleEngine(
        steps = mutableListOf<WeightedCoachingBundleAdjuster>(
            FlaggedWeightedCoachingBundleAdjuster(flags, "weighted-coaching-bundle-default", -4.3),
            WeightedCoachingBundleCeilingOffsetAdjuster(1.11),
            TieredWeightedCoachingBundleAdjuster(
                mutableListOf<Double>(4446.0, 5412.0, 6118.0, 7537.0),
                mutableListOf<Double>(0.95, 1.03, 1.09, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 52587.5,
    )
}

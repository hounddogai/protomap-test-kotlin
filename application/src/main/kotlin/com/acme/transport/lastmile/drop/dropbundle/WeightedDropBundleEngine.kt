package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted drop bundle calculation. */
interface WeightedDropBundleStep {
    fun describe(): String
    fun adjust(item: WeightedDropBundle, value: Double): Double
}

data class PercentWeightedDropBundleAdjuster(var percent: Double) : WeightedDropBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDropBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedDropBundleAdjuster(var minimum: Double) : WeightedDropBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDropBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedDropBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDropBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDropBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedDropBundleAdjuster(var step: Double) : WeightedDropBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDropBundle, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedDropBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDropBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDropBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted drop bundle steps in order and keeps a trace of each. */
data class WeightedDropBundleEngine(
    var steps: MutableList<WeightedDropBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedDropBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDropBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDropBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedDropBundleEngine(flags: Registry): WeightedDropBundleEngine {
    return WeightedDropBundleEngine(
        steps = mutableListOf<WeightedDropBundleStep>(
            PercentWeightedDropBundleAdjuster(20.3),
            MinimumWeightedDropBundleAdjuster(23.1),
            TieredWeightedDropBundleAdjuster(
                mutableListOf<Double>(4733.0, 5663.0, 6745.0),
                mutableListOf<Double>(0.94, 0.98, 0.88),
            ),
            RoundingWeightedDropBundleAdjuster(0.1),
            FlaggedWeightedDropBundleAdjuster(flags, "weighted-drop-bundle-regional", -3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 99917.7,
    )
}

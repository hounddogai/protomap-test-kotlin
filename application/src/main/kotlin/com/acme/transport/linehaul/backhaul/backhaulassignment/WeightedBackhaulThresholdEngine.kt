package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted backhaul threshold calculation. */
interface WeightedBackhaulThresholdModifier {
    fun describe(): String
    fun adjust(item: WeightedBackhaulThreshold, value: Double): Double
}

data class CapWeightedBackhaulThresholdAdjuster(var cap: Double) : WeightedBackhaulThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedBackhaulThreshold, value: Double): Double = minOf(value, cap)
}

data class WeightedBackhaulThresholdUpdatedAtOffsetAdjuster(
    var weight: Double,
) : WeightedBackhaulThresholdModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: WeightedBackhaulThreshold, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class RoundingWeightedBackhaulThresholdAdjuster(var step: Double) : WeightedBackhaulThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedBackhaulThreshold, value: Double): Double = roundTo(value, step)
}

data class CountryWeightedBackhaulThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedBackhaulThresholdModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: WeightedBackhaulThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedBackhaulThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedBackhaulThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedBackhaulThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedBackhaulThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedBackhaulThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedBackhaulThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted backhaul threshold steps in order and keeps a trace of each. */
data class WeightedBackhaulThresholdEngine(
    var steps: MutableList<WeightedBackhaulThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedBackhaulThreshold): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedBackhaulThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedBackhaulThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedBackhaulThresholdEngine(flags: Registry): WeightedBackhaulThresholdEngine {
    return WeightedBackhaulThresholdEngine(
        steps = mutableListOf<WeightedBackhaulThresholdModifier>(
            CapWeightedBackhaulThresholdAdjuster(40850.0),
            WeightedBackhaulThresholdUpdatedAtOffsetAdjuster(0.64),
            RoundingWeightedBackhaulThresholdAdjuster(0.25),
            CountryWeightedBackhaulThresholdAdjuster(mutableMapOf<String, Double>("north" to 1.4)),
            FlaggedWeightedBackhaulThresholdAdjuster(flags, "weighted-backhaul-threshold-manual", 2.9),
            TieredWeightedBackhaulThresholdAdjuster(
                mutableListOf<Double>(1267.0, 3182.0, 8296.0, 8440.0),
                mutableListOf<Double>(1.0, 1.11, 1.01, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 50872.0,
    )
}

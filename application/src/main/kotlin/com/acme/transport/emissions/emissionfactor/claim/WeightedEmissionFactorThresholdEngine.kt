package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.flags.Registry

/** One step of the weighted emission factor threshold calculation. */
interface WeightedEmissionFactorThresholdFactor {
    fun describe(): String
    fun adjust(item: WeightedEmissionFactorThreshold, value: Double): Double
}

data class CapWeightedEmissionFactorThresholdAdjuster(var cap: Double) : WeightedEmissionFactorThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedEmissionFactorThreshold, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedEmissionFactorThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedEmissionFactorThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedEmissionFactorThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryWeightedEmissionFactorThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedEmissionFactorThresholdFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedEmissionFactorThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted emission factor threshold steps in order and keeps a trace of each. */
data class WeightedEmissionFactorThresholdEngine(
    var steps: MutableList<WeightedEmissionFactorThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedEmissionFactorThreshold): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedEmissionFactorThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedEmissionFactorThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedEmissionFactorThresholdEngine(flags: Registry): WeightedEmissionFactorThresholdEngine {
    return WeightedEmissionFactorThresholdEngine(
        steps = mutableListOf<WeightedEmissionFactorThresholdFactor>(
            CapWeightedEmissionFactorThresholdAdjuster(16472.0),
            FlaggedWeightedEmissionFactorThresholdAdjuster(
                flags,
                "weighted-emission-factor-threshold-rolling",
                -0.5,
            ),
            CategoryWeightedEmissionFactorThresholdAdjuster(
                mutableMapOf<String, Double>("east" to 0.77, "online" to 1.4, "south" to 0.72),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 44575.9,
    )
}

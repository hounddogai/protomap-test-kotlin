package com.acme.transport.network.node.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted node threshold calculation. */
interface WeightedNodeThresholdAdjuster {
    fun describe(): String
    fun adjust(item: WeightedNodeThreshold, value: Double): Double
}

data class RoundingWeightedNodeThresholdAdjuster(var step: Double) : WeightedNodeThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedNodeThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedNodeThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedNodeThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedNodeThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedNodeThresholdStartsAtOffsetAdjuster(var weight: Double) : WeightedNodeThresholdAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: WeightedNodeThreshold, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class UnitWeightedNodeThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedNodeThresholdAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: WeightedNodeThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedNodeThresholdAdjuster(var percent: Double) : WeightedNodeThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedNodeThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedNodeThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedNodeThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedNodeThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted node threshold steps in order and keeps a trace of each. */
data class WeightedNodeThresholdEngine(
    var steps: MutableList<WeightedNodeThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedNodeThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedNodeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedNodeThresholdEngine(flags: Registry): WeightedNodeThresholdEngine {
    return WeightedNodeThresholdEngine(
        steps = mutableListOf<WeightedNodeThresholdAdjuster>(
            RoundingWeightedNodeThresholdAdjuster(1.0),
            FlaggedWeightedNodeThresholdAdjuster(flags, "weighted-node-threshold-strict", 2.9),
            WeightedNodeThresholdStartsAtOffsetAdjuster(0.57),
            UnitWeightedNodeThresholdAdjuster(mutableMapOf<String, Double>("central" to 1.17, "east" to 0.94)),
            PercentWeightedNodeThresholdAdjuster(24.7),
            TieredWeightedNodeThresholdAdjuster(
                mutableListOf<Double>(177.0, 701.0, 4993.0, 7355.0),
                mutableListOf<Double>(1.16, 0.8, 1.03, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 41656.0,
    )
}

package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive utilization rollup calculation. */
interface AdaptiveUtilizationRollupModifier {
    fun describe(): String
    fun adjust(item: AdaptiveUtilizationRollup, value: Double): Double
}

data class CapAdaptiveUtilizationRollupAdjuster(var cap: Double) : AdaptiveUtilizationRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveUtilizationRollup, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveUtilizationRollupAdjuster(var step: Double) : AdaptiveUtilizationRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveUtilizationRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveUtilizationRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveUtilizationRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveUtilizationRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveUtilizationRollupPriorityOffsetAdjuster(
    var weight: Double,
) : AdaptiveUtilizationRollupModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: AdaptiveUtilizationRollup, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the adaptive utilization rollup steps in order and keeps a trace of each. */
data class AdaptiveUtilizationRollupEngine(
    var steps: MutableList<AdaptiveUtilizationRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveUtilizationRollup): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveUtilizationRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveUtilizationRollupEngine(flags: Registry): AdaptiveUtilizationRollupEngine {
    return AdaptiveUtilizationRollupEngine(
        steps = mutableListOf<AdaptiveUtilizationRollupModifier>(
            CapAdaptiveUtilizationRollupAdjuster(29635.0),
            RoundingAdaptiveUtilizationRollupAdjuster(5.0),
            FlaggedAdaptiveUtilizationRollupAdjuster(flags, "adaptive-utilization-rollup-legacy", -2.1),
            AdaptiveUtilizationRollupPriorityOffsetAdjuster(1.14),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 37981.4,
    )
}

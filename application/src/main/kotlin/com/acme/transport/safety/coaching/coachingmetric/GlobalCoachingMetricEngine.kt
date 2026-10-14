package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global coaching metric calculation. */
interface GlobalCoachingMetricModifier {
    fun describe(): String
    fun adjust(item: GlobalCoachingMetric, value: Double): Double
}

data class PercentGlobalCoachingMetricAdjuster(var percent: Double) : GlobalCoachingMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCoachingMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalCoachingMetricPalletsOffsetAdjuster(var weight: Double) : GlobalCoachingMetricModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: GlobalCoachingMetric, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class FlaggedGlobalCoachingMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCoachingMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCoachingMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalCoachingMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCoachingMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCoachingMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global coaching metric steps in order and keeps a trace of each. */
data class GlobalCoachingMetricEngine(
    var steps: MutableList<GlobalCoachingMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCoachingMetric): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCoachingMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCoachingMetricEngine(flags: Registry): GlobalCoachingMetricEngine {
    return GlobalCoachingMetricEngine(
        steps = mutableListOf<GlobalCoachingMetricModifier>(
            PercentGlobalCoachingMetricAdjuster(17.1),
            GlobalCoachingMetricPalletsOffsetAdjuster(0.22),
            FlaggedGlobalCoachingMetricAdjuster(flags, "global-coaching-metric-seasonal", 4.7),
            TieredGlobalCoachingMetricAdjuster(
                mutableListOf<Double>(2311.0, 2804.0, 6963.0, 7963.0),
                mutableListOf<Double>(1.2, 0.93, 1.05, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 61834.9,
    )
}

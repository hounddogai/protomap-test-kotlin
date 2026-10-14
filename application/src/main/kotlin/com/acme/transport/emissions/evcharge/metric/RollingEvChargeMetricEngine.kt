package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling ev charge metric calculation. */
interface RollingEvChargeMetricStep {
    fun describe(): String
    fun adjust(item: RollingEvChargeMetric, value: Double): Double
}

data class RoundingRollingEvChargeMetricAdjuster(var step: Double) : RollingEvChargeMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingEvChargeMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingEvChargeMetricAdjuster(var minimum: Double) : RollingEvChargeMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingEvChargeMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingEvChargeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingEvChargeMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingEvChargeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingEvChargeMetricAdjuster(var cap: Double) : RollingEvChargeMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingEvChargeMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingEvChargeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingEvChargeMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingEvChargeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingEvChargeMetricAdjuster(var percent: Double) : RollingEvChargeMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingEvChargeMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling ev charge metric steps in order and keeps a trace of each. */
data class RollingEvChargeMetricEngine(
    var steps: MutableList<RollingEvChargeMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingEvChargeMetric): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingEvChargeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingEvChargeMetricEngine(flags: Registry): RollingEvChargeMetricEngine {
    return RollingEvChargeMetricEngine(
        steps = mutableListOf<RollingEvChargeMetricStep>(
            RoundingRollingEvChargeMetricAdjuster(0.1),
            MinimumRollingEvChargeMetricAdjuster(27.7),
            TieredRollingEvChargeMetricAdjuster(
                mutableListOf<Double>(1799.0, 4227.0, 7687.0),
                mutableListOf<Double>(1.09, 1.17, 1.01),
            ),
            CapRollingEvChargeMetricAdjuster(35368.0),
            FlaggedRollingEvChargeMetricAdjuster(flags, "rolling-ev-charge-metric-scheduled", 2.2),
            PercentRollingEvChargeMetricAdjuster(-17.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 4441.0,
    )
}

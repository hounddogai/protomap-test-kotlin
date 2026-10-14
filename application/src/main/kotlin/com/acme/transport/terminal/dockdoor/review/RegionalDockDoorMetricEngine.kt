package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional dock door metric calculation. */
interface RegionalDockDoorMetricFactor {
    fun describe(): String
    fun adjust(item: RegionalDockDoorMetric, value: Double): Double
}

data class CountryRegionalDockDoorMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDockDoorMetricFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: RegionalDockDoorMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalDockDoorMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDockDoorMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDockDoorMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalDockDoorMetricAdjuster(var minimum: Double) : RegionalDockDoorMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDockDoorMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRegionalDockDoorMetricAdjuster(var cap: Double) : RegionalDockDoorMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDockDoorMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalDockDoorMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDockDoorMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDockDoorMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalDockDoorMetricAdjuster(var step: Double) : RegionalDockDoorMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDockDoorMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the regional dock door metric steps in order and keeps a trace of each. */
data class RegionalDockDoorMetricEngine(
    var steps: MutableList<RegionalDockDoorMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalDockDoorMetric): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDockDoorMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDockDoorMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalDockDoorMetricEngine(flags: Registry): RegionalDockDoorMetricEngine {
    return RegionalDockDoorMetricEngine(
        steps = mutableListOf<RegionalDockDoorMetricFactor>(
            CountryRegionalDockDoorMetricAdjuster(mutableMapOf<String, Double>("central" to 1.2)),
            TieredRegionalDockDoorMetricAdjuster(
                mutableListOf<Double>(873.0, 2444.0),
                mutableListOf<Double>(0.88, 0.98),
            ),
            MinimumRegionalDockDoorMetricAdjuster(0.9),
            CapRegionalDockDoorMetricAdjuster(22837.0),
            FlaggedRegionalDockDoorMetricAdjuster(flags, "regional-dock-door-metric-pending", -4.5),
            RoundingRegionalDockDoorMetricAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 76633.5,
    )
}

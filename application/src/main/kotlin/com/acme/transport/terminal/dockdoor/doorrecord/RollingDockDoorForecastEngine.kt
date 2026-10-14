package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.flags.Registry

/** One step of the rolling dock door forecast calculation. */
interface RollingDockDoorForecastModifier {
    fun describe(): String
    fun adjust(item: RollingDockDoorForecast, value: Double): Double
}

data class LabelRollingDockDoorForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDockDoorForecastModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingDockDoorForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingDockDoorForecastAdjuster(var cap: Double) : RollingDockDoorForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDockDoorForecast, value: Double): Double = minOf(value, cap)
}

data class PercentRollingDockDoorForecastAdjuster(var percent: Double) : RollingDockDoorForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDockDoorForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingDockDoorForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDockDoorForecastModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDockDoorForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingDockDoorForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDockDoorForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDockDoorForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling dock door forecast steps in order and keeps a trace of each. */
data class RollingDockDoorForecastEngine(
    var steps: MutableList<RollingDockDoorForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingDockDoorForecast): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDockDoorForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDockDoorForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingDockDoorForecastEngine(flags: Registry): RollingDockDoorForecastEngine {
    return RollingDockDoorForecastEngine(
        steps = mutableListOf<RollingDockDoorForecastModifier>(
            LabelRollingDockDoorForecastAdjuster(
                mutableMapOf<String, Double>("south" to 1.19, "online" to 0.97, "west" to 1.16),
            ),
            CapRollingDockDoorForecastAdjuster(24538.0),
            PercentRollingDockDoorForecastAdjuster(8.0),
            FlaggedRollingDockDoorForecastAdjuster(flags, "rolling-dock-door-forecast-tiered", -0.7),
            TieredRollingDockDoorForecastAdjuster(
                mutableListOf<Double>(4340.0, 5193.0, 5353.0),
                mutableListOf<Double>(0.87, 1.0, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 9189.9,
    )
}

package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the dock door forecast calculation. */
interface DockDoorForecastAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorForecast, value: Double): Double
}

data class MinimumDockDoorForecastAdjuster(var minimum: Double) : DockDoorForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDockDoorForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDockDoorForecastAdjuster(var percent: Double) : DockDoorForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dock door forecast steps in order and keeps a trace of each. */
data class DockDoorForecastEngine(
    var steps: MutableList<DockDoorForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorForecast): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorForecastEngine(flags: Registry): DockDoorForecastEngine {
    return DockDoorForecastEngine(
        steps = mutableListOf<DockDoorForecastAdjuster>(
            MinimumDockDoorForecastAdjuster(41.7),
            FlaggedDockDoorForecastAdjuster(flags, "dock-door-forecast-capped", -2.9),
            PercentDockDoorForecastAdjuster(12.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 64481.2,
    )
}

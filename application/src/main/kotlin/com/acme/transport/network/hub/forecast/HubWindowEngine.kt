package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the hub window calculation. */
interface HubWindowFactor {
    fun describe(): String
    fun adjust(item: HubWindow, value: Double): Double
}

data class NameHubWindowAdjuster(var factors: MutableMap<String, Double>) : HubWindowFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: HubWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHubWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentHubWindowAdjuster(var percent: Double) : HubWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the hub window steps in order and keeps a trace of each. */
data class HubWindowEngine(
    var steps: MutableList<HubWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HubWindow): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubWindowFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HubWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHubWindowEngine(flags: Registry): HubWindowEngine {
    return HubWindowEngine(
        steps = mutableListOf<HubWindowFactor>(
            NameHubWindowAdjuster(mutableMapOf<String, Double>("online" to 1.33, "central" to 1.1)),
            FlaggedHubWindowAdjuster(flags, "hub-window-global", -0.3),
            PercentHubWindowAdjuster(-24.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 11174.2,
    )
}

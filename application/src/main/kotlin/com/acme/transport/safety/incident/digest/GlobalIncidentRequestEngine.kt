package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.flags.Registry

/** One step of the global incident request calculation. */
interface GlobalIncidentRequestModifier {
    fun describe(): String
    fun adjust(item: GlobalIncidentRequest, value: Double): Double
}

data class MinimumGlobalIncidentRequestAdjuster(var minimum: Double) : GlobalIncidentRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalIncidentRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalIncidentRequestFuelLitersOffsetAdjuster(var weight: Double) : GlobalIncidentRequestModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: GlobalIncidentRequest, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedGlobalIncidentRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalIncidentRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalIncidentRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalIncidentRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalIncidentRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalIncidentRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global incident request steps in order and keeps a trace of each. */
data class GlobalIncidentRequestEngine(
    var steps: MutableList<GlobalIncidentRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalIncidentRequest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalIncidentRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalIncidentRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalIncidentRequestEngine(flags: Registry): GlobalIncidentRequestEngine {
    return GlobalIncidentRequestEngine(
        steps = mutableListOf<GlobalIncidentRequestModifier>(
            MinimumGlobalIncidentRequestAdjuster(42.6),
            GlobalIncidentRequestFuelLitersOffsetAdjuster(0.73),
            FlaggedGlobalIncidentRequestAdjuster(flags, "global-incident-request-legacy", 5.8),
            TieredGlobalIncidentRequestAdjuster(
                mutableListOf<Double>(1693.0, 3034.0, 3569.0, 9835.0),
                mutableListOf<Double>(0.87, 0.8, 0.86, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 32672.0,
    )
}

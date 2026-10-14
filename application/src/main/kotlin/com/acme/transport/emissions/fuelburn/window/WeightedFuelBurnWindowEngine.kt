package com.acme.transport.emissions.fuelburn.window

/** One step of the weighted fuel burn window calculation. */
interface WeightedFuelBurnWindowStep {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnWindow, value: Double): Double
}

data class TieredWeightedFuelBurnWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedFuelBurnWindowAdjuster(var minimum: Double) : WeightedFuelBurnWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelBurnWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameWeightedFuelBurnWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedFuelBurnWindowStep {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedFuelBurnWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted fuel burn window steps in order and keeps a trace of each. */
data class WeightedFuelBurnWindowEngine(
    var steps: MutableList<WeightedFuelBurnWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedFuelBurnWindow): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedFuelBurnWindowStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedFuelBurnWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnWindowEngine(): WeightedFuelBurnWindowEngine {
    return WeightedFuelBurnWindowEngine(
        steps = mutableListOf<WeightedFuelBurnWindowStep>(
            TieredWeightedFuelBurnWindowAdjuster(
                mutableListOf<Double>(3555.0, 6143.0),
                mutableListOf<Double>(1.06, 0.95),
            ),
            MinimumWeightedFuelBurnWindowAdjuster(32.6),
            NameWeightedFuelBurnWindowAdjuster(mutableMapOf<String, Double>("central" to 1.13, "east" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 67291.7,
    )
}

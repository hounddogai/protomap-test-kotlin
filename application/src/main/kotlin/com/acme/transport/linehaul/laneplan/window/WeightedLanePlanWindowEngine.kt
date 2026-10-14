package com.acme.transport.linehaul.laneplan.window

/** One step of the weighted lane plan window calculation. */
interface WeightedLanePlanWindowModifier {
    fun describe(): String
    fun adjust(item: WeightedLanePlanWindow, value: Double): Double
}

data class TieredWeightedLanePlanWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedLanePlanWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedLanePlanWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedLanePlanWindowAdjuster(var percent: Double) : WeightedLanePlanWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedLanePlanWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedLanePlanWindowAdjuster(var minimum: Double) : WeightedLanePlanWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedLanePlanWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedLanePlanWindowFuelLitersOffsetAdjuster(var weight: Double) : WeightedLanePlanWindowModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: WeightedLanePlanWindow, value: Double): Double = value + item.fuelLiters * weight
}

data class CapWeightedLanePlanWindowAdjuster(var cap: Double) : WeightedLanePlanWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedLanePlanWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted lane plan window steps in order and keeps a trace of each. */
data class WeightedLanePlanWindowEngine(
    var steps: MutableList<WeightedLanePlanWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedLanePlanWindow): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedLanePlanWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedLanePlanWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedLanePlanWindowEngine(): WeightedLanePlanWindowEngine {
    return WeightedLanePlanWindowEngine(
        steps = mutableListOf<WeightedLanePlanWindowModifier>(
            TieredWeightedLanePlanWindowAdjuster(
                mutableListOf<Double>(980.0, 1709.0, 7414.0, 8287.0),
                mutableListOf<Double>(1.12, 0.93, 0.87, 1.05),
            ),
            PercentWeightedLanePlanWindowAdjuster(2.8),
            MinimumWeightedLanePlanWindowAdjuster(38.4),
            WeightedLanePlanWindowFuelLitersOffsetAdjuster(1.11),
            CapWeightedLanePlanWindowAdjuster(31006.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 73115.5,
    )
}

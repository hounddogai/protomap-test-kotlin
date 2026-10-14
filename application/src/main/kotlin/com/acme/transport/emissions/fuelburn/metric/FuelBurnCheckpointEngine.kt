package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn checkpoint calculation. */
interface FuelBurnCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnCheckpoint, value: Double): Double
}

data class RoundingFuelBurnCheckpointAdjuster(var step: Double) : FuelBurnCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentFuelBurnCheckpointAdjuster(var percent: Double) : FuelBurnCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFuelBurnCheckpointAdjuster(var minimum: Double) : FuelBurnCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryFuelBurnCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelBurnCheckpointAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: FuelBurnCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel burn checkpoint steps in order and keeps a trace of each. */
data class FuelBurnCheckpointEngine(
    var steps: MutableList<FuelBurnCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnCheckpoint): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnCheckpointEngine(): FuelBurnCheckpointEngine {
    return FuelBurnCheckpointEngine(
        steps = mutableListOf<FuelBurnCheckpointAdjuster>(
            RoundingFuelBurnCheckpointAdjuster(0.05),
            PercentFuelBurnCheckpointAdjuster(8.2),
            MinimumFuelBurnCheckpointAdjuster(3.9),
            CategoryFuelBurnCheckpointAdjuster(
                mutableMapOf<String, Double>("west" to 1.11, "retail" to 0.8, "central" to 1.25),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 76499.9,
    )
}

package com.acme.transport.emissions.fuelburn.request

/** One step of the fuel burn cycle calculation. */
interface FuelBurnCycleFactor {
    fun describe(): String
    fun adjust(item: FuelBurnCycle, value: Double): Double
}

data class FuelBurnCycleCapacityOffsetAdjuster(var weight: Double) : FuelBurnCycleFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: FuelBurnCycle, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class CapFuelBurnCycleAdjuster(var cap: Double) : FuelBurnCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnCycle, value: Double): Double = minOf(value, cap)
}

data class TieredFuelBurnCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFuelBurnCycleAdjuster(var minimum: Double) : FuelBurnCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitFuelBurnCycleAdjuster(var factors: MutableMap<String, Double>) : FuelBurnCycleFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: FuelBurnCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel burn cycle steps in order and keeps a trace of each. */
data class FuelBurnCycleEngine(
    var steps: MutableList<FuelBurnCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnCycle): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnCycleEngine(): FuelBurnCycleEngine {
    return FuelBurnCycleEngine(
        steps = mutableListOf<FuelBurnCycleFactor>(
            FuelBurnCycleCapacityOffsetAdjuster(1.92),
            CapFuelBurnCycleAdjuster(19962.0),
            TieredFuelBurnCycleAdjuster(
                mutableListOf<Double>(3227.0, 4115.0, 4394.0, 7775.0),
                mutableListOf<Double>(0.83, 1.18, 0.9, 1.13),
            ),
            MinimumFuelBurnCycleAdjuster(13.8),
            UnitFuelBurnCycleAdjuster(mutableMapOf<String, Double>("south" to 1.05)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 83522.2,
    )
}

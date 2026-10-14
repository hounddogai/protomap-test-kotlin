package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn profile calculation. */
interface FuelBurnProfileFactor {
    fun describe(): String
    fun adjust(item: FuelBurnProfile, value: Double): Double
}

data class FuelBurnProfileStartsAtOffsetAdjuster(var weight: Double) : FuelBurnProfileFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FuelBurnProfile, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class CapFuelBurnProfileAdjuster(var cap: Double) : FuelBurnProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnProfile, value: Double): Double = minOf(value, cap)
}

data class TieredFuelBurnProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFuelBurnProfileAdjuster(var step: Double) : FuelBurnProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel burn profile steps in order and keeps a trace of each. */
data class FuelBurnProfileEngine(
    var steps: MutableList<FuelBurnProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnProfile): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnProfileEngine(): FuelBurnProfileEngine {
    return FuelBurnProfileEngine(
        steps = mutableListOf<FuelBurnProfileFactor>(
            FuelBurnProfileStartsAtOffsetAdjuster(1.14),
            CapFuelBurnProfileAdjuster(44640.0),
            TieredFuelBurnProfileAdjuster(
                mutableListOf<Double>(97.0, 4719.0, 4783.0),
                mutableListOf<Double>(0.81, 1.06, 1.14),
            ),
            RoundingFuelBurnProfileAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 10881.3,
    )
}

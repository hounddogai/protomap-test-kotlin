package com.acme.transport.emissions.emissionfactor.manifest

/** One step of the emission factor slot calculation. */
interface EmissionFactorSlotStep {
    fun describe(): String
    fun adjust(item: EmissionFactorSlot, value: Double): Double
}

data class TieredEmissionFactorSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapEmissionFactorSlotAdjuster(var cap: Double) : EmissionFactorSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorSlot, value: Double): Double = minOf(value, cap)
}

data class EmissionFactorSlotAmountOffsetAdjuster(var weight: Double) : EmissionFactorSlotStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: EmissionFactorSlot, value: Double): Double = value + item.amount * weight
}

/** Runs the emission factor slot steps in order and keeps a trace of each. */
data class EmissionFactorSlotEngine(
    var steps: MutableList<EmissionFactorSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorSlot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorSlotEngine(): EmissionFactorSlotEngine {
    return EmissionFactorSlotEngine(
        steps = mutableListOf<EmissionFactorSlotStep>(
            TieredEmissionFactorSlotAdjuster(
                mutableListOf<Double>(606.0, 7150.0, 7755.0, 9665.0),
                mutableListOf<Double>(0.87, 1.2, 0.91, 0.81),
            ),
            CapEmissionFactorSlotAdjuster(24293.0),
            EmissionFactorSlotAmountOffsetAdjuster(0.75),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 44696.6,
    )
}

package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor cycle calculation. */
interface EmissionFactorCycleFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorCycle, value: Double): Double
}

data class RoundingEmissionFactorCycleAdjuster(var step: Double) : EmissionFactorCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorCycle, value: Double): Double = roundTo(value, step)
}

data class TieredEmissionFactorCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapEmissionFactorCycleAdjuster(var cap: Double) : EmissionFactorCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorCycle, value: Double): Double = minOf(value, cap)
}

data class PercentEmissionFactorCycleAdjuster(var percent: Double) : EmissionFactorCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the emission factor cycle steps in order and keeps a trace of each. */
data class EmissionFactorCycleEngine(
    var steps: MutableList<EmissionFactorCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorCycle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorCycleEngine(): EmissionFactorCycleEngine {
    return EmissionFactorCycleEngine(
        steps = mutableListOf<EmissionFactorCycleFactor>(
            RoundingEmissionFactorCycleAdjuster(0.05),
            TieredEmissionFactorCycleAdjuster(
                mutableListOf<Double>(542.0, 3825.0, 9209.0),
                mutableListOf<Double>(0.84, 1.13, 0.95),
            ),
            CapEmissionFactorCycleAdjuster(33347.0),
            PercentEmissionFactorCycleAdjuster(-7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 77536.7,
    )
}

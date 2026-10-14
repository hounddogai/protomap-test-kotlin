package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor target calculation. */
interface EmissionFactorTargetStep {
    fun describe(): String
    fun adjust(item: EmissionFactorTarget, value: Double): Double
}

data class PercentEmissionFactorTargetAdjuster(var percent: Double) : EmissionFactorTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class EmissionFactorTargetStartsAtOffsetAdjuster(var weight: Double) : EmissionFactorTargetStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: EmissionFactorTarget, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class MinimumEmissionFactorTargetAdjuster(var minimum: Double) : EmissionFactorTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the emission factor target steps in order and keeps a trace of each. */
data class EmissionFactorTargetEngine(
    var steps: MutableList<EmissionFactorTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorTarget): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorTargetEngine(): EmissionFactorTargetEngine {
    return EmissionFactorTargetEngine(
        steps = mutableListOf<EmissionFactorTargetStep>(
            PercentEmissionFactorTargetAdjuster(-5.6),
            EmissionFactorTargetStartsAtOffsetAdjuster(1.97),
            MinimumEmissionFactorTargetAdjuster(11.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 23720.5,
    )
}

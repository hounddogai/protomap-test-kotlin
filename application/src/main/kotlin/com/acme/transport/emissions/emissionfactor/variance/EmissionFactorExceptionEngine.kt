package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor exception calculation. */
interface EmissionFactorExceptionModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorException, value: Double): Double
}

data class CapEmissionFactorExceptionAdjuster(var cap: Double) : EmissionFactorExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorException, value: Double): Double = minOf(value, cap)
}

data class RoundingEmissionFactorExceptionAdjuster(var step: Double) : EmissionFactorExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorException, value: Double): Double = roundTo(value, step)
}

data class EmissionFactorExceptionMinimumOffsetAdjuster(var weight: Double) : EmissionFactorExceptionModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: EmissionFactorException, value: Double): Double = value + item.minimum * weight
}

data class FlaggedEmissionFactorExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the emission factor exception steps in order and keeps a trace of each. */
data class EmissionFactorExceptionEngine(
    var steps: MutableList<EmissionFactorExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorException): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorExceptionEngine(flags: Registry): EmissionFactorExceptionEngine {
    return EmissionFactorExceptionEngine(
        steps = mutableListOf<EmissionFactorExceptionModifier>(
            CapEmissionFactorExceptionAdjuster(1138.0),
            RoundingEmissionFactorExceptionAdjuster(0.25),
            EmissionFactorExceptionMinimumOffsetAdjuster(1.52),
            FlaggedEmissionFactorExceptionAdjuster(flags, "emission-factor-exception-batched", -5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 72654.8,
    )
}

package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient emission factor request calculation. */
interface LenientEmissionFactorRequestStep {
    fun describe(): String
    fun adjust(item: LenientEmissionFactorRequest, value: Double): Double
}

data class TieredLenientEmissionFactorRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientEmissionFactorRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientEmissionFactorRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientEmissionFactorRequestAdjuster(var step: Double) : LenientEmissionFactorRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientEmissionFactorRequest, value: Double): Double = roundTo(value, step)
}

data class CapLenientEmissionFactorRequestAdjuster(var cap: Double) : LenientEmissionFactorRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientEmissionFactorRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient emission factor request steps in order and keeps a trace of each. */
data class LenientEmissionFactorRequestEngine(
    var steps: MutableList<LenientEmissionFactorRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientEmissionFactorRequest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientEmissionFactorRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientEmissionFactorRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientEmissionFactorRequestEngine(): LenientEmissionFactorRequestEngine {
    return LenientEmissionFactorRequestEngine(
        steps = mutableListOf<LenientEmissionFactorRequestStep>(
            TieredLenientEmissionFactorRequestAdjuster(
                mutableListOf<Double>(450.0, 2905.0, 5987.0),
                mutableListOf<Double>(1.13, 1.09, 1.11),
            ),
            RoundingLenientEmissionFactorRequestAdjuster(0.5),
            CapLenientEmissionFactorRequestAdjuster(48635.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 65699.9,
    )
}

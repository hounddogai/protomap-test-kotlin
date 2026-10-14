package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor draft calculation. */
interface EmissionFactorDraftStep {
    fun describe(): String
    fun adjust(item: EmissionFactorDraft, value: Double): Double
}

data class EmissionFactorDraftDiscountOffsetAdjuster(var weight: Double) : EmissionFactorDraftStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: EmissionFactorDraft, value: Double): Double = value + item.discount * weight
}

data class RoundingEmissionFactorDraftAdjuster(var step: Double) : EmissionFactorDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorDraft, value: Double): Double = roundTo(value, step)
}

data class UnitEmissionFactorDraftAdjuster(var factors: MutableMap<String, Double>) : EmissionFactorDraftStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: EmissionFactorDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor draft steps in order and keeps a trace of each. */
data class EmissionFactorDraftEngine(
    var steps: MutableList<EmissionFactorDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorDraft): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorDraftEngine(): EmissionFactorDraftEngine {
    return EmissionFactorDraftEngine(
        steps = mutableListOf<EmissionFactorDraftStep>(
            EmissionFactorDraftDiscountOffsetAdjuster(0.98),
            RoundingEmissionFactorDraftAdjuster(0.5),
            UnitEmissionFactorDraftAdjuster(
                mutableMapOf<String, Double>("online" to 1.09, "south" to 1.17, "central" to 0.71),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 70761.0,
    )
}

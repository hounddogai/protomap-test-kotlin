package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive zone agreement calculation. */
interface AdaptiveZoneAgreementStep {
    fun describe(): String
    fun adjust(item: AdaptiveZoneAgreement, value: Double): Double
}

data class RoundingAdaptiveZoneAgreementAdjuster(var step: Double) : AdaptiveZoneAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveZoneAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveZoneAgreementAdjuster(var minimum: Double) : AdaptiveZoneAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveZoneAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveZoneAgreementUpdatedAtOffsetAdjuster(var weight: Double) : AdaptiveZoneAgreementStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: AdaptiveZoneAgreement, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

/** Runs the adaptive zone agreement steps in order and keeps a trace of each. */
data class AdaptiveZoneAgreementEngine(
    var steps: MutableList<AdaptiveZoneAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveZoneAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveZoneAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveZoneAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveZoneAgreementEngine(): AdaptiveZoneAgreementEngine {
    return AdaptiveZoneAgreementEngine(
        steps = mutableListOf<AdaptiveZoneAgreementStep>(
            RoundingAdaptiveZoneAgreementAdjuster(1.0),
            MinimumAdaptiveZoneAgreementAdjuster(2.9),
            AdaptiveZoneAgreementUpdatedAtOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 52818.1,
    )
}

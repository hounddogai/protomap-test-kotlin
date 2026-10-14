package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive emission factor request calculation. */
interface AdaptiveEmissionFactorRequestStep {
    fun describe(): String
    fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double
}

data class RoundingAdaptiveEmissionFactorRequestAdjuster(var step: Double) : AdaptiveEmissionFactorRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double = roundTo(value, step)
}

data class AdaptiveEmissionFactorRequestRevisionOffsetAdjuster(
    var weight: Double,
) : AdaptiveEmissionFactorRequestStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class PercentAdaptiveEmissionFactorRequestAdjuster(
    var percent: Double,
) : AdaptiveEmissionFactorRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveEmissionFactorRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveEmissionFactorRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveEmissionFactorRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveEmissionFactorRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleAdaptiveEmissionFactorRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveEmissionFactorRequestStep {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveEmissionFactorRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive emission factor request steps in order and keeps a trace of each. */
data class AdaptiveEmissionFactorRequestEngine(
    var steps: MutableList<AdaptiveEmissionFactorRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveEmissionFactorRequest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEmissionFactorRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEmissionFactorRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveEmissionFactorRequestEngine(flags: Registry): AdaptiveEmissionFactorRequestEngine {
    return AdaptiveEmissionFactorRequestEngine(
        steps = mutableListOf<AdaptiveEmissionFactorRequestStep>(
            RoundingAdaptiveEmissionFactorRequestAdjuster(0.05),
            AdaptiveEmissionFactorRequestRevisionOffsetAdjuster(1.04),
            PercentAdaptiveEmissionFactorRequestAdjuster(-12.4),
            FlaggedAdaptiveEmissionFactorRequestAdjuster(
                flags,
                "adaptive-emission-factor-request-default",
                -0.2,
            ),
            TieredAdaptiveEmissionFactorRequestAdjuster(
                mutableListOf<Double>(1596.0, 3425.0),
                mutableListOf<Double>(1.18, 1.09),
            ),
            TitleAdaptiveEmissionFactorRequestAdjuster(
                mutableMapOf<String, Double>("central" to 0.73, "west" to 0.73),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 27860.5,
    )
}

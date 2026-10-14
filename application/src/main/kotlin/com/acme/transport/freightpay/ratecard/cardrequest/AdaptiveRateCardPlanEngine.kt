package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive rate card plan calculation. */
interface AdaptiveRateCardPlanStep {
    fun describe(): String
    fun adjust(item: AdaptiveRateCardPlan, value: Double): Double
}

data class FlaggedAdaptiveRateCardPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveRateCardPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveRateCardPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveRateCardPlanRetriesOffsetAdjuster(var weight: Double) : AdaptiveRateCardPlanStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AdaptiveRateCardPlan, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentAdaptiveRateCardPlanAdjuster(var percent: Double) : AdaptiveRateCardPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveRateCardPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveRateCardPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRateCardPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRateCardPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveRateCardPlanAdjuster(var minimum: Double) : AdaptiveRateCardPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRateCardPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveRateCardPlanAdjuster(var step: Double) : AdaptiveRateCardPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRateCardPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive rate card plan steps in order and keeps a trace of each. */
data class AdaptiveRateCardPlanEngine(
    var steps: MutableList<AdaptiveRateCardPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveRateCardPlan): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveRateCardPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveRateCardPlanEngine(flags: Registry): AdaptiveRateCardPlanEngine {
    return AdaptiveRateCardPlanEngine(
        steps = mutableListOf<AdaptiveRateCardPlanStep>(
            FlaggedAdaptiveRateCardPlanAdjuster(flags, "adaptive-rate-card-plan-lenient", 9.0),
            AdaptiveRateCardPlanRetriesOffsetAdjuster(0.8),
            PercentAdaptiveRateCardPlanAdjuster(-7.1),
            TieredAdaptiveRateCardPlanAdjuster(
                mutableListOf<Double>(1830.0, 4663.0, 6145.0, 6997.0),
                mutableListOf<Double>(0.98, 1.02, 1.03, 0.86),
            ),
            MinimumAdaptiveRateCardPlanAdjuster(23.0),
            RoundingAdaptiveRateCardPlanAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 8951.3,
    )
}

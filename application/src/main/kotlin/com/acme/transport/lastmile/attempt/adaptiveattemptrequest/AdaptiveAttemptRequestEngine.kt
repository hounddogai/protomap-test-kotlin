package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive attempt request calculation. */
interface AdaptiveAttemptRequestStep {
    fun describe(): String
    fun adjust(item: AdaptiveAttemptRequest, value: Double): Double
}

data class MinimumAdaptiveAttemptRequestAdjuster(var minimum: Double) : AdaptiveAttemptRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAttemptRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelAdaptiveAttemptRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAttemptRequestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveAttemptRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveAttemptRequestAdjuster(var percent: Double) : AdaptiveAttemptRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAttemptRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveAttemptRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAttemptRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAttemptRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveAttemptRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAttemptRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAttemptRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveAttemptRequestAdjuster(var step: Double) : AdaptiveAttemptRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAttemptRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive attempt request steps in order and keeps a trace of each. */
data class AdaptiveAttemptRequestEngine(
    var steps: MutableList<AdaptiveAttemptRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveAttemptRequest): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveAttemptRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveAttemptRequestEngine(flags: Registry): AdaptiveAttemptRequestEngine {
    return AdaptiveAttemptRequestEngine(
        steps = mutableListOf<AdaptiveAttemptRequestStep>(
            MinimumAdaptiveAttemptRequestAdjuster(35.3),
            LabelAdaptiveAttemptRequestAdjuster(mutableMapOf<String, Double>("central" to 0.93)),
            PercentAdaptiveAttemptRequestAdjuster(18.6),
            FlaggedAdaptiveAttemptRequestAdjuster(flags, "adaptive-attempt-request-primary", -5.3),
            TieredAdaptiveAttemptRequestAdjuster(
                mutableListOf<Double>(6544.0, 8164.0, 9192.0, 9196.0),
                mutableListOf<Double>(0.99, 1.19, 0.93, 1.13),
            ),
            RoundingAdaptiveAttemptRequestAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 1930.4,
    )
}

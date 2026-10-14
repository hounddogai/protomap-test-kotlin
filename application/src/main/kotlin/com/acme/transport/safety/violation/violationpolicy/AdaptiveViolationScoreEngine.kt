package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive violation score calculation. */
interface AdaptiveViolationScoreStep {
    fun describe(): String
    fun adjust(item: AdaptiveViolationScore, value: Double): Double
}

data class LaneAdaptiveViolationScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveViolationScoreStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: AdaptiveViolationScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAdaptiveViolationScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveViolationScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveViolationScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveViolationScoreAdjuster(var minimum: Double) : AdaptiveViolationScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveViolationScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveViolationScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveViolationScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveViolationScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive violation score steps in order and keeps a trace of each. */
data class AdaptiveViolationScoreEngine(
    var steps: MutableList<AdaptiveViolationScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveViolationScore): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveViolationScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveViolationScoreEngine(flags: Registry): AdaptiveViolationScoreEngine {
    return AdaptiveViolationScoreEngine(
        steps = mutableListOf<AdaptiveViolationScoreStep>(
            LaneAdaptiveViolationScoreAdjuster(mutableMapOf<String, Double>("south" to 0.76)),
            FlaggedAdaptiveViolationScoreAdjuster(flags, "adaptive-violation-score-capped", -5.3),
            MinimumAdaptiveViolationScoreAdjuster(26.8),
            TieredAdaptiveViolationScoreAdjuster(
                mutableListOf<Double>(3560.0, 3710.0, 6732.0),
                mutableListOf<Double>(0.83, 1.11, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 6866.9,
    )
}

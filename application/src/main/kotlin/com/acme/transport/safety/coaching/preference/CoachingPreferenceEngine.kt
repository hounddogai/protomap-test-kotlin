package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching preference calculation. */
interface CoachingPreferenceModifier {
    fun describe(): String
    fun adjust(item: CoachingPreference, value: Double): Double
}

data class RoundingCoachingPreferenceAdjuster(var step: Double) : CoachingPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingPreference, value: Double): Double = roundTo(value, step)
}

data class ChannelCoachingPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : CoachingPreferenceModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: CoachingPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCoachingPreferenceAdjuster(var percent: Double) : CoachingPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCoachingPreferenceAdjuster(var minimum: Double) : CoachingPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CoachingPreferenceDiscountOffsetAdjuster(var weight: Double) : CoachingPreferenceModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CoachingPreference, value: Double): Double = value + item.discount * weight
}

data class CapCoachingPreferenceAdjuster(var cap: Double) : CoachingPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the coaching preference steps in order and keeps a trace of each. */
data class CoachingPreferenceEngine(
    var steps: MutableList<CoachingPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingPreference): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingPreferenceEngine(): CoachingPreferenceEngine {
    return CoachingPreferenceEngine(
        steps = mutableListOf<CoachingPreferenceModifier>(
            RoundingCoachingPreferenceAdjuster(5.0),
            ChannelCoachingPreferenceAdjuster(mutableMapOf<String, Double>("central" to 1.12, "south" to 1.08)),
            PercentCoachingPreferenceAdjuster(-6.1),
            MinimumCoachingPreferenceAdjuster(21.4),
            CoachingPreferenceDiscountOffsetAdjuster(1.24),
            CapCoachingPreferenceAdjuster(12017.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 74005.6,
    )
}

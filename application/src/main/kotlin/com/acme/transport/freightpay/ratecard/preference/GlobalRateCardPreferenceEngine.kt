package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global rate card preference calculation. */
interface GlobalRateCardPreferenceStep {
    fun describe(): String
    fun adjust(item: GlobalRateCardPreference, value: Double): Double
}

data class RoundingGlobalRateCardPreferenceAdjuster(var step: Double) : GlobalRateCardPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRateCardPreference, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalRateCardPreferenceAdjuster(var percent: Double) : GlobalRateCardPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRateCardPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalRateCardPreferenceRevisionOffsetAdjuster(var weight: Double) : GlobalRateCardPreferenceStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalRateCardPreference, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class MinimumGlobalRateCardPreferenceAdjuster(var minimum: Double) : GlobalRateCardPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRateCardPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedGlobalRateCardPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRateCardPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRateCardPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalRateCardPreferenceAdjuster(var cap: Double) : GlobalRateCardPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRateCardPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the global rate card preference steps in order and keeps a trace of each. */
data class GlobalRateCardPreferenceEngine(
    var steps: MutableList<GlobalRateCardPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRateCardPreference): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRateCardPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRateCardPreferenceEngine(flags: Registry): GlobalRateCardPreferenceEngine {
    return GlobalRateCardPreferenceEngine(
        steps = mutableListOf<GlobalRateCardPreferenceStep>(
            RoundingGlobalRateCardPreferenceAdjuster(5.0),
            PercentGlobalRateCardPreferenceAdjuster(6.1),
            GlobalRateCardPreferenceRevisionOffsetAdjuster(0.12),
            MinimumGlobalRateCardPreferenceAdjuster(9.5),
            FlaggedGlobalRateCardPreferenceAdjuster(flags, "global-rate-card-preference-default", -10.0),
            CapGlobalRateCardPreferenceAdjuster(26744.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 54932.4,
    )
}

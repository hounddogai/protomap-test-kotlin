package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary tender preference calculation. */
interface PrimaryTenderPreferenceStep {
    fun describe(): String
    fun adjust(item: PrimaryTenderPreference, value: Double): Double
}

data class MinimumPrimaryTenderPreferenceAdjuster(var minimum: Double) : PrimaryTenderPreferenceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTenderPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryTenderPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTenderPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTenderPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPrimaryTenderPreferenceAdjuster(var cap: Double) : PrimaryTenderPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTenderPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryTenderPreferenceAdjuster(var step: Double) : PrimaryTenderPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTenderPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryTenderPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTenderPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTenderPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocalePrimaryTenderPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTenderPreferenceStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryTenderPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary tender preference steps in order and keeps a trace of each. */
data class PrimaryTenderPreferenceEngine(
    var steps: MutableList<PrimaryTenderPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTenderPreference): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTenderPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTenderPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTenderPreferenceEngine(flags: Registry): PrimaryTenderPreferenceEngine {
    return PrimaryTenderPreferenceEngine(
        steps = mutableListOf<PrimaryTenderPreferenceStep>(
            MinimumPrimaryTenderPreferenceAdjuster(40.2),
            TieredPrimaryTenderPreferenceAdjuster(
                mutableListOf<Double>(3076.0, 4613.0, 5664.0, 7430.0),
                mutableListOf<Double>(1.03, 0.95, 0.9, 1.07),
            ),
            CapPrimaryTenderPreferenceAdjuster(18020.0),
            RoundingPrimaryTenderPreferenceAdjuster(0.25),
            FlaggedPrimaryTenderPreferenceAdjuster(flags, "primary-tender-preference-seasonal", -5.6),
            LocalePrimaryTenderPreferenceAdjuster(
                mutableMapOf<String, Double>("east" to 0.85, "central" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 25245.1,
    )
}

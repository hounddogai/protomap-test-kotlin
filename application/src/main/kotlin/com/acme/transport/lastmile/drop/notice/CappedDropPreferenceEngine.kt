package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped drop preference calculation. */
interface CappedDropPreferenceStep {
    fun describe(): String
    fun adjust(item: CappedDropPreference, value: Double): Double
}

data class TieredCappedDropPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDropPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDropPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedDropPreferenceAdjuster(var cap: Double) : CappedDropPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDropPreference, value: Double): Double = minOf(value, cap)
}

data class CappedDropPreferencePalletsOffsetAdjuster(var weight: Double) : CappedDropPreferenceStep {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CappedDropPreference, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class RoundingCappedDropPreferenceAdjuster(var step: Double) : CappedDropPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDropPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedDropPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDropPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDropPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped drop preference steps in order and keeps a trace of each. */
data class CappedDropPreferenceEngine(
    var steps: MutableList<CappedDropPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedDropPreference): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDropPreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDropPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedDropPreferenceEngine(flags: Registry): CappedDropPreferenceEngine {
    return CappedDropPreferenceEngine(
        steps = mutableListOf<CappedDropPreferenceStep>(
            TieredCappedDropPreferenceAdjuster(
                mutableListOf<Double>(682.0, 944.0, 2403.0, 5503.0),
                mutableListOf<Double>(0.85, 0.94, 0.99, 0.97),
            ),
            CapCappedDropPreferenceAdjuster(36704.0),
            CappedDropPreferencePalletsOffsetAdjuster(0.14),
            RoundingCappedDropPreferenceAdjuster(0.25),
            FlaggedCappedDropPreferenceAdjuster(flags, "capped-drop-preference-capped", 4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 50908.5,
    )
}

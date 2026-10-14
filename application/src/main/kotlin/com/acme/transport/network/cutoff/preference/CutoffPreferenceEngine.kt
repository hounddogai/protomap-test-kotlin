package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff preference calculation. */
interface CutoffPreferenceStep {
    fun describe(): String
    fun adjust(item: CutoffPreference, value: Double): Double
}

data class RoundingCutoffPreferenceAdjuster(var step: Double) : CutoffPreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffPreference, value: Double): Double = roundTo(value, step)
}

data class CapCutoffPreferenceAdjuster(var cap: Double) : CutoffPreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffPreference, value: Double): Double = minOf(value, cap)
}

data class FlaggedCutoffPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffPreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleCutoffPreferenceAdjuster(var factors: MutableMap<String, Double>) : CutoffPreferenceStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CutoffPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cutoff preference steps in order and keeps a trace of each. */
data class CutoffPreferenceEngine(
    var steps: MutableList<CutoffPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffPreference): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffPreferenceEngine(flags: Registry): CutoffPreferenceEngine {
    return CutoffPreferenceEngine(
        steps = mutableListOf<CutoffPreferenceStep>(
            RoundingCutoffPreferenceAdjuster(1.0),
            CapCutoffPreferenceAdjuster(29384.0),
            FlaggedCutoffPreferenceAdjuster(flags, "cutoff-preference-adaptive", 9.5),
            TitleCutoffPreferenceAdjuster(mutableMapOf<String, Double>("north" to 0.74)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 38201.0,
    )
}

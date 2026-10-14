package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled cutoff preference calculation. */
interface ScheduledCutoffPreferenceFactor {
    fun describe(): String
    fun adjust(item: ScheduledCutoffPreference, value: Double): Double
}

data class FlaggedScheduledCutoffPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledCutoffPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledCutoffPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledCutoffPreferenceAdjuster(var cap: Double) : ScheduledCutoffPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledCutoffPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingScheduledCutoffPreferenceAdjuster(var step: Double) : ScheduledCutoffPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledCutoffPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled cutoff preference steps in order and keeps a trace of each. */
data class ScheduledCutoffPreferenceEngine(
    var steps: MutableList<ScheduledCutoffPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledCutoffPreference): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledCutoffPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledCutoffPreferenceEngine(flags: Registry): ScheduledCutoffPreferenceEngine {
    return ScheduledCutoffPreferenceEngine(
        steps = mutableListOf<ScheduledCutoffPreferenceFactor>(
            FlaggedScheduledCutoffPreferenceAdjuster(flags, "scheduled-cutoff-preference-fallback", -7.5),
            CapScheduledCutoffPreferenceAdjuster(17962.0),
            RoundingScheduledCutoffPreferenceAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 5267.5,
    )
}

package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss preference calculation. */
interface NearMissPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: NearMissPreference, value: Double): Double
}

data class TieredNearMissPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NearMissPreferenceMaximumOffsetAdjuster(var weight: Double) : NearMissPreferenceAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: NearMissPreference, value: Double): Double = value + item.maximum * weight
}

data class RoundingNearMissPreferenceAdjuster(var step: Double) : NearMissPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissPreference, value: Double): Double = roundTo(value, step)
}

data class CapNearMissPreferenceAdjuster(var cap: Double) : NearMissPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissPreference, value: Double): Double = minOf(value, cap)
}

data class FlaggedNearMissPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the near miss preference steps in order and keeps a trace of each. */
data class NearMissPreferenceEngine(
    var steps: MutableList<NearMissPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NearMissPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNearMissPreferenceEngine(flags: Registry): NearMissPreferenceEngine {
    return NearMissPreferenceEngine(
        steps = mutableListOf<NearMissPreferenceAdjuster>(
            TieredNearMissPreferenceAdjuster(
                mutableListOf<Double>(4632.0, 9285.0, 9707.0),
                mutableListOf<Double>(1.18, 0.86, 0.85),
            ),
            NearMissPreferenceMaximumOffsetAdjuster(1.61),
            RoundingNearMissPreferenceAdjuster(1.0),
            CapNearMissPreferenceAdjuster(13036.0),
            FlaggedNearMissPreferenceAdjuster(flags, "near-miss-preference-scheduled", -1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 68457.9,
    )
}

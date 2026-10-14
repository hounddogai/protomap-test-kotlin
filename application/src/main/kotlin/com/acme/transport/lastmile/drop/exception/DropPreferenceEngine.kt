package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop preference calculation. */
interface DropPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: DropPreference, value: Double): Double
}

data class MinimumDropPreferenceAdjuster(var minimum: Double) : DropPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDropPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDropPreferenceAdjuster(var step: Double) : DropPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedDropPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the drop preference steps in order and keeps a trace of each. */
data class DropPreferenceEngine(
    var steps: MutableList<DropPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropPreference): Double {
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

    fun evaluateAll(items: MutableList<DropPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropPreferenceEngine(flags: Registry): DropPreferenceEngine {
    return DropPreferenceEngine(
        steps = mutableListOf<DropPreferenceAdjuster>(
            MinimumDropPreferenceAdjuster(39.4),
            TieredDropPreferenceAdjuster(
                mutableListOf<Double>(4561.0, 6673.0),
                mutableListOf<Double>(1.08, 0.83),
            ),
            RoundingDropPreferenceAdjuster(0.5),
            FlaggedDropPreferenceAdjuster(flags, "drop-preference-adaptive", 4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 81037.9,
    )
}

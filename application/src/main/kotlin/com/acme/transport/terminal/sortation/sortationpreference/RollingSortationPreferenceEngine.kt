package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling sortation preference calculation. */
interface RollingSortationPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: RollingSortationPreference, value: Double): Double
}

data class ChannelRollingSortationPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingSortationPreferenceAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: RollingSortationPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRollingSortationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingSortationPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingSortationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRollingSortationPreferenceAdjuster(var minimum: Double) : RollingSortationPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingSortationPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingSortationPreferenceAdjuster(var step: Double) : RollingSortationPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingSortationPreference, value: Double): Double = roundTo(value, step)
}

data class CapRollingSortationPreferenceAdjuster(var cap: Double) : RollingSortationPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingSortationPreference, value: Double): Double = minOf(value, cap)
}

data class TieredRollingSortationPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingSortationPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingSortationPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling sortation preference steps in order and keeps a trace of each. */
data class RollingSortationPreferenceEngine(
    var steps: MutableList<RollingSortationPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingSortationPreference): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingSortationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingSortationPreferenceEngine(flags: Registry): RollingSortationPreferenceEngine {
    return RollingSortationPreferenceEngine(
        steps = mutableListOf<RollingSortationPreferenceAdjuster>(
            ChannelRollingSortationPreferenceAdjuster(
                mutableMapOf<String, Double>("south" to 1.39, "north" to 1.05, "west" to 0.89),
            ),
            FlaggedRollingSortationPreferenceAdjuster(flags, "rolling-sortation-preference-batched", -9.1),
            MinimumRollingSortationPreferenceAdjuster(8.4),
            RoundingRollingSortationPreferenceAdjuster(0.5),
            CapRollingSortationPreferenceAdjuster(9882.0),
            TieredRollingSortationPreferenceAdjuster(
                mutableListOf<Double>(343.0, 4240.0, 6225.0, 8930.0),
                mutableListOf<Double>(1.09, 0.86, 1.15, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 17383.1,
    )
}

package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay preference calculation. */
interface RelayPreferenceFactor {
    fun describe(): String
    fun adjust(item: RelayPreference, value: Double): Double
}

data class LabelRelayPreferenceAdjuster(var factors: MutableMap<String, Double>) : RelayPreferenceFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RelayPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRelayPreferenceAdjuster(var cap: Double) : RelayPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayPreference, value: Double): Double = minOf(value, cap)
}

data class MinimumRelayPreferenceAdjuster(var minimum: Double) : RelayPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRelayPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRelayPreferenceAdjuster(var percent: Double) : RelayPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RelayPreferenceScoreOffsetAdjuster(var weight: Double) : RelayPreferenceFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RelayPreference, value: Double): Double = value + item.score * weight
}

/** Runs the relay preference steps in order and keeps a trace of each. */
data class RelayPreferenceEngine(
    var steps: MutableList<RelayPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayPreference): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayPreferenceEngine(): RelayPreferenceEngine {
    return RelayPreferenceEngine(
        steps = mutableListOf<RelayPreferenceFactor>(
            LabelRelayPreferenceAdjuster(
                mutableMapOf<String, Double>("west" to 1.35, "east" to 1.36, "retail" to 1.04),
            ),
            CapRelayPreferenceAdjuster(28085.0),
            MinimumRelayPreferenceAdjuster(47.0),
            TieredRelayPreferenceAdjuster(
                mutableListOf<Double>(6224.0, 6833.0, 8296.0),
                mutableListOf<Double>(0.96, 0.82, 1.13),
            ),
            PercentRelayPreferenceAdjuster(-3.9),
            RelayPreferenceScoreOffsetAdjuster(0.76),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 86777.4,
    )
}

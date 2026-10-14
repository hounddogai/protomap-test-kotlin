package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending relay preference calculation. */
interface PendingRelayPreferenceFactor {
    fun describe(): String
    fun adjust(item: PendingRelayPreference, value: Double): Double
}

data class TieredPendingRelayPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingRelayPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingRelayPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingRelayPreferenceAdjuster(var minimum: Double) : PendingRelayPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRelayPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingRelayPreferenceAdjuster(var cap: Double) : PendingRelayPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRelayPreference, value: Double): Double = minOf(value, cap)
}

data class NamePendingRelayPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRelayPreferenceFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: PendingRelayPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingRelayPreferenceCostOffsetAdjuster(var weight: Double) : PendingRelayPreferenceFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: PendingRelayPreference, value: Double): Double = value + item.cost * weight
}

data class RoundingPendingRelayPreferenceAdjuster(var step: Double) : PendingRelayPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRelayPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the pending relay preference steps in order and keeps a trace of each. */
data class PendingRelayPreferenceEngine(
    var steps: MutableList<PendingRelayPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingRelayPreference): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRelayPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRelayPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingRelayPreferenceEngine(): PendingRelayPreferenceEngine {
    return PendingRelayPreferenceEngine(
        steps = mutableListOf<PendingRelayPreferenceFactor>(
            TieredPendingRelayPreferenceAdjuster(
                mutableListOf<Double>(411.0, 650.0, 7901.0, 8231.0),
                mutableListOf<Double>(1.11, 1.19, 0.93, 1.05),
            ),
            MinimumPendingRelayPreferenceAdjuster(35.9),
            CapPendingRelayPreferenceAdjuster(17473.0),
            NamePendingRelayPreferenceAdjuster(
                mutableMapOf<String, Double>("retail" to 0.74, "central" to 1.12),
            ),
            PendingRelayPreferenceCostOffsetAdjuster(0.7),
            RoundingPendingRelayPreferenceAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 14515.0,
    )
}

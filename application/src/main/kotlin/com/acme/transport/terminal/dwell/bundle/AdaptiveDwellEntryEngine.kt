package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive dwell entry calculation. */
interface AdaptiveDwellEntryStep {
    fun describe(): String
    fun adjust(item: AdaptiveDwellEntry, value: Double): Double
}

data class AdaptiveDwellEntryStopCountOffsetAdjuster(var weight: Double) : AdaptiveDwellEntryStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: AdaptiveDwellEntry, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class MinimumAdaptiveDwellEntryAdjuster(var minimum: Double) : AdaptiveDwellEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDwellEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameAdaptiveDwellEntryAdjuster(var factors: MutableMap<String, Double>) : AdaptiveDwellEntryStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveDwellEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAdaptiveDwellEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDwellEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDwellEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveDwellEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDwellEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDwellEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveDwellEntryAdjuster(var cap: Double) : AdaptiveDwellEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDwellEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive dwell entry steps in order and keeps a trace of each. */
data class AdaptiveDwellEntryEngine(
    var steps: MutableList<AdaptiveDwellEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveDwellEntry): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDwellEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDwellEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveDwellEntryEngine(flags: Registry): AdaptiveDwellEntryEngine {
    return AdaptiveDwellEntryEngine(
        steps = mutableListOf<AdaptiveDwellEntryStep>(
            AdaptiveDwellEntryStopCountOffsetAdjuster(1.61),
            MinimumAdaptiveDwellEntryAdjuster(14.0),
            NameAdaptiveDwellEntryAdjuster(
                mutableMapOf<String, Double>("north" to 1.02, "west" to 0.79, "online" to 0.98),
            ),
            FlaggedAdaptiveDwellEntryAdjuster(flags, "adaptive-dwell-entry-manual", 0.3),
            TieredAdaptiveDwellEntryAdjuster(
                mutableListOf<Double>(1281.0, 1412.0, 5542.0, 6385.0),
                mutableListOf<Double>(1.01, 0.84, 1.06, 0.88),
            ),
            CapAdaptiveDwellEntryAdjuster(34487.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 34021.9,
    )
}

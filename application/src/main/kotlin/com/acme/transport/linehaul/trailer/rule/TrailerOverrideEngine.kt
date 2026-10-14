package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.Registry

/** One step of the trailer override calculation. */
interface TrailerOverrideStep {
    fun describe(): String
    fun adjust(item: TrailerOverride, value: Double): Double
}

data class FlaggedTrailerOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTrailerOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTrailerOverrideAdjuster(var percent: Double) : TrailerOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TrailerOverrideWindowDaysOffsetAdjuster(var weight: Double) : TrailerOverrideStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: TrailerOverride, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class CapTrailerOverrideAdjuster(var cap: Double) : TrailerOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerOverride, value: Double): Double = minOf(value, cap)
}

data class UnitTrailerOverrideAdjuster(var factors: MutableMap<String, Double>) : TrailerOverrideStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: TrailerOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer override steps in order and keeps a trace of each. */
data class TrailerOverrideEngine(
    var steps: MutableList<TrailerOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerOverride): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerOverrideEngine(flags: Registry): TrailerOverrideEngine {
    return TrailerOverrideEngine(
        steps = mutableListOf<TrailerOverrideStep>(
            FlaggedTrailerOverrideAdjuster(flags, "trailer-override-global", -7.6),
            TieredTrailerOverrideAdjuster(
                mutableListOf<Double>(1140.0, 2641.0, 9255.0),
                mutableListOf<Double>(0.95, 0.95, 1.15),
            ),
            PercentTrailerOverrideAdjuster(-3.8),
            TrailerOverrideWindowDaysOffsetAdjuster(1.79),
            CapTrailerOverrideAdjuster(24414.0),
            UnitTrailerOverrideAdjuster(
                mutableMapOf<String, Double>("east" to 0.82, "central" to 0.72, "online" to 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 13007.0,
    )
}

package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional dwell profile calculation. */
interface RegionalDwellProfileStep {
    fun describe(): String
    fun adjust(item: RegionalDwellProfile, value: Double): Double
}

data class CapRegionalDwellProfileAdjuster(var cap: Double) : RegionalDwellProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDwellProfile, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalDwellProfileAdjuster(var minimum: Double) : RegionalDwellProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDwellProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentRegionalDwellProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDwellProfileStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: RegionalDwellProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalDwellProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDwellProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDwellProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalDwellProfileAdjuster(var step: Double) : RegionalDwellProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDwellProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the regional dwell profile steps in order and keeps a trace of each. */
data class RegionalDwellProfileEngine(
    var steps: MutableList<RegionalDwellProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDwellProfile): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDwellProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDwellProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDwellProfileEngine(): RegionalDwellProfileEngine {
    return RegionalDwellProfileEngine(
        steps = mutableListOf<RegionalDwellProfileStep>(
            CapRegionalDwellProfileAdjuster(33231.0),
            MinimumRegionalDwellProfileAdjuster(0.9),
            SegmentRegionalDwellProfileAdjuster(
                mutableMapOf<String, Double>("retail" to 1.17, "west" to 1.3, "east" to 1.25),
            ),
            TieredRegionalDwellProfileAdjuster(
                mutableListOf<Double>(361.0, 1123.0, 4818.0, 9432.0),
                mutableListOf<Double>(0.93, 1.15, 1.05, 1.01),
            ),
            RoundingRegionalDwellProfileAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 33025.8,
    )
}

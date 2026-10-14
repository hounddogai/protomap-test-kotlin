package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell rollup calculation. */
interface DwellRollupModifier {
    fun describe(): String
    fun adjust(item: DwellRollup, value: Double): Double
}

data class TieredDwellRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDwellRollupAdjuster(var minimum: Double) : DwellRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDwellRollupAdjuster(var step: Double) : DwellRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellRollup, value: Double): Double = roundTo(value, step)
}

data class CapDwellRollupAdjuster(var cap: Double) : DwellRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellRollup, value: Double): Double = minOf(value, cap)
}

data class PercentDwellRollupAdjuster(var percent: Double) : DwellRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dwell rollup steps in order and keeps a trace of each. */
data class DwellRollupEngine(
    var steps: MutableList<DwellRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellRollup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DwellRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellRollupEngine(): DwellRollupEngine {
    return DwellRollupEngine(
        steps = mutableListOf<DwellRollupModifier>(
            TieredDwellRollupAdjuster(
                mutableListOf<Double>(404.0, 5064.0, 8754.0),
                mutableListOf<Double>(1.16, 0.85, 1.1),
            ),
            MinimumDwellRollupAdjuster(47.5),
            RoundingDwellRollupAdjuster(0.05),
            CapDwellRollupAdjuster(1050.0),
            PercentDwellRollupAdjuster(22.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 69301.4,
    )
}

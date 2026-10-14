package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict cutoff rollup calculation. */
interface StrictCutoffRollupFactor {
    fun describe(): String
    fun adjust(item: StrictCutoffRollup, value: Double): Double
}

data class DepotStrictCutoffRollupAdjuster(var factors: MutableMap<String, Double>) : StrictCutoffRollupFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: StrictCutoffRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictCutoffRollupAdjuster(var minimum: Double) : StrictCutoffRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCutoffRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictCutoffRollupAdjuster(var step: Double) : StrictCutoffRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCutoffRollup, value: Double): Double = roundTo(value, step)
}

data class PercentStrictCutoffRollupAdjuster(var percent: Double) : StrictCutoffRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictCutoffRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict cutoff rollup steps in order and keeps a trace of each. */
data class StrictCutoffRollupEngine(
    var steps: MutableList<StrictCutoffRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictCutoffRollup): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCutoffRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCutoffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictCutoffRollupEngine(): StrictCutoffRollupEngine {
    return StrictCutoffRollupEngine(
        steps = mutableListOf<StrictCutoffRollupFactor>(
            DepotStrictCutoffRollupAdjuster(
                mutableMapOf<String, Double>("west" to 0.89, "online" to 1.11, "north" to 1.34),
            ),
            MinimumStrictCutoffRollupAdjuster(13.0),
            RoundingStrictCutoffRollupAdjuster(0.1),
            PercentStrictCutoffRollupAdjuster(-7.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 96219.3,
    )
}

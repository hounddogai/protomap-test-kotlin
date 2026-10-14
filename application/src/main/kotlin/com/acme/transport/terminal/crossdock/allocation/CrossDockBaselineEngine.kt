package com.acme.transport.terminal.crossdock.allocation

/** One step of the cross dock baseline calculation. */
interface CrossDockBaselineModifier {
    fun describe(): String
    fun adjust(item: CrossDockBaseline, value: Double): Double
}

data class CrossDockBaselineEndsAtOffsetAdjuster(var weight: Double) : CrossDockBaselineModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CrossDockBaseline, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class MinimumCrossDockBaselineAdjuster(var minimum: Double) : CrossDockBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCrossDockBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCrossDockBaselineAdjuster(var cap: Double) : CrossDockBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockBaseline, value: Double): Double = minOf(value, cap)
}

data class NameCrossDockBaselineAdjuster(var factors: MutableMap<String, Double>) : CrossDockBaselineModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CrossDockBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cross dock baseline steps in order and keeps a trace of each. */
data class CrossDockBaselineEngine(
    var steps: MutableList<CrossDockBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CrossDockBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCrossDockBaselineEngine(): CrossDockBaselineEngine {
    return CrossDockBaselineEngine(
        steps = mutableListOf<CrossDockBaselineModifier>(
            CrossDockBaselineEndsAtOffsetAdjuster(0.14),
            MinimumCrossDockBaselineAdjuster(10.3),
            TieredCrossDockBaselineAdjuster(
                mutableListOf<Double>(3382.0, 5380.0),
                mutableListOf<Double>(1.11, 1.11),
            ),
            CapCrossDockBaselineAdjuster(42447.0),
            NameCrossDockBaselineAdjuster(mutableMapOf<String, Double>("retail" to 1.17)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 55274.5,
    )
}

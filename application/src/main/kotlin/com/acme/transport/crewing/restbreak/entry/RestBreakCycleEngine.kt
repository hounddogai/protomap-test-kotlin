package com.acme.transport.crewing.restbreak.entry

/** One step of the rest break cycle calculation. */
interface RestBreakCycleAdjuster {
    fun describe(): String
    fun adjust(item: RestBreakCycle, value: Double): Double
}

data class CapRestBreakCycleAdjuster(var cap: Double) : RestBreakCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakCycle, value: Double): Double = minOf(value, cap)
}

data class LaneRestBreakCycleAdjuster(var factors: MutableMap<String, Double>) : RestBreakCycleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RestBreakCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRestBreakCycleAdjuster(var percent: Double) : RestBreakCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRestBreakCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rest break cycle steps in order and keeps a trace of each. */
data class RestBreakCycleEngine(
    var steps: MutableList<RestBreakCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakCycle): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakCycleEngine(): RestBreakCycleEngine {
    return RestBreakCycleEngine(
        steps = mutableListOf<RestBreakCycleAdjuster>(
            CapRestBreakCycleAdjuster(25200.0),
            LaneRestBreakCycleAdjuster(
                mutableMapOf<String, Double>("west" to 1.32, "south" to 1.33, "east" to 1.36),
            ),
            PercentRestBreakCycleAdjuster(-13.3),
            TieredRestBreakCycleAdjuster(
                mutableListOf<Double>(1768.0, 3046.0, 4378.0, 6923.0),
                mutableListOf<Double>(1.16, 1.17, 0.87, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 31250.5,
    )
}

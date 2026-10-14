package com.acme.transport.customs.broker.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict broker schedule calculation. */
interface StrictBrokerScheduleFactor {
    fun describe(): String
    fun adjust(item: StrictBrokerSchedule, value: Double): Double
}

data class MinimumStrictBrokerScheduleAdjuster(var minimum: Double) : StrictBrokerScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictBrokerSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictBrokerScheduleAdjuster(var step: Double) : StrictBrokerScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictBrokerSchedule, value: Double): Double = roundTo(value, step)
}

data class CategoryStrictBrokerScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictBrokerScheduleFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: StrictBrokerSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictBrokerScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictBrokerScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictBrokerSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict broker schedule steps in order and keeps a trace of each. */
data class StrictBrokerScheduleEngine(
    var steps: MutableList<StrictBrokerScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictBrokerSchedule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictBrokerScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictBrokerSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictBrokerScheduleEngine(): StrictBrokerScheduleEngine {
    return StrictBrokerScheduleEngine(
        steps = mutableListOf<StrictBrokerScheduleFactor>(
            MinimumStrictBrokerScheduleAdjuster(8.0),
            RoundingStrictBrokerScheduleAdjuster(5.0),
            CategoryStrictBrokerScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 1.21, "retail" to 1.26),
            ),
            TieredStrictBrokerScheduleAdjuster(
                mutableListOf<Double>(1127.0, 2140.0, 2647.0),
                mutableListOf<Double>(0.98, 1.1, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 89590.8,
    )
}

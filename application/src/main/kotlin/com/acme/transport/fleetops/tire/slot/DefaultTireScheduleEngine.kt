package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the default tire schedule calculation. */
interface DefaultTireScheduleFactor {
    fun describe(): String
    fun adjust(item: DefaultTireSchedule, value: Double): Double
}

data class CapDefaultTireScheduleAdjuster(var cap: Double) : DefaultTireScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTireSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultTireScheduleAdjuster(var percent: Double) : DefaultTireScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTireSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotDefaultTireScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTireScheduleFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: DefaultTireSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefaultTireScheduleDiscountOffsetAdjuster(var weight: Double) : DefaultTireScheduleFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DefaultTireSchedule, value: Double): Double = value + item.discount * weight
}

data class RoundingDefaultTireScheduleAdjuster(var step: Double) : DefaultTireScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTireSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the default tire schedule steps in order and keeps a trace of each. */
data class DefaultTireScheduleEngine(
    var steps: MutableList<DefaultTireScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultTireSchedule): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTireScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTireSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultTireScheduleEngine(): DefaultTireScheduleEngine {
    return DefaultTireScheduleEngine(
        steps = mutableListOf<DefaultTireScheduleFactor>(
            CapDefaultTireScheduleAdjuster(49632.0),
            PercentDefaultTireScheduleAdjuster(-16.6),
            DepotDefaultTireScheduleAdjuster(mutableMapOf<String, Double>("north" to 0.96, "west" to 0.96)),
            DefaultTireScheduleDiscountOffsetAdjuster(1.99),
            RoundingDefaultTireScheduleAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 31087.4,
    )
}

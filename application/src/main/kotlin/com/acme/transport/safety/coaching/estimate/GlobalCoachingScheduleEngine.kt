package com.acme.transport.safety.coaching.estimate

/** One step of the global coaching schedule calculation. */
interface GlobalCoachingScheduleModifier {
    fun describe(): String
    fun adjust(item: GlobalCoachingSchedule, value: Double): Double
}

data class CapGlobalCoachingScheduleAdjuster(var cap: Double) : GlobalCoachingScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCoachingSchedule, value: Double): Double = minOf(value, cap)
}

data class SegmentGlobalCoachingScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCoachingScheduleModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalCoachingSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredGlobalCoachingScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCoachingScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCoachingSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalCoachingScheduleAdjuster(var minimum: Double) : GlobalCoachingScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCoachingSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalCoachingScheduleCapacityOffsetAdjuster(var weight: Double) : GlobalCoachingScheduleModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: GlobalCoachingSchedule, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

/** Runs the global coaching schedule steps in order and keeps a trace of each. */
data class GlobalCoachingScheduleEngine(
    var steps: MutableList<GlobalCoachingScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalCoachingSchedule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCoachingScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCoachingSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalCoachingScheduleEngine(): GlobalCoachingScheduleEngine {
    return GlobalCoachingScheduleEngine(
        steps = mutableListOf<GlobalCoachingScheduleModifier>(
            CapGlobalCoachingScheduleAdjuster(9986.0),
            SegmentGlobalCoachingScheduleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.4, "central" to 0.89, "south" to 0.91),
            ),
            TieredGlobalCoachingScheduleAdjuster(
                mutableListOf<Double>(215.0, 4628.0, 5267.0, 9811.0),
                mutableListOf<Double>(0.94, 0.94, 0.91, 0.81),
            ),
            MinimumGlobalCoachingScheduleAdjuster(6.9),
            GlobalCoachingScheduleCapacityOffsetAdjuster(1.87),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 83507.1,
    )
}

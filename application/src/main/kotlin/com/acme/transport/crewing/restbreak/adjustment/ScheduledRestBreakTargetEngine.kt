package com.acme.transport.crewing.restbreak.adjustment

/** One step of the scheduled rest break target calculation. */
interface ScheduledRestBreakTargetStep {
    fun describe(): String
    fun adjust(item: ScheduledRestBreakTarget, value: Double): Double
}

data class CapScheduledRestBreakTargetAdjuster(var cap: Double) : ScheduledRestBreakTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRestBreakTarget, value: Double): Double = minOf(value, cap)
}

data class TitleScheduledRestBreakTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRestBreakTargetStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledRestBreakTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledRestBreakTargetAdjuster(var minimum: Double) : ScheduledRestBreakTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRestBreakTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledRestBreakTargetOdometerOffsetAdjuster(var weight: Double) : ScheduledRestBreakTargetStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: ScheduledRestBreakTarget, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

/** Runs the scheduled rest break target steps in order and keeps a trace of each. */
data class ScheduledRestBreakTargetEngine(
    var steps: MutableList<ScheduledRestBreakTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRestBreakTarget): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRestBreakTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRestBreakTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRestBreakTargetEngine(): ScheduledRestBreakTargetEngine {
    return ScheduledRestBreakTargetEngine(
        steps = mutableListOf<ScheduledRestBreakTargetStep>(
            CapScheduledRestBreakTargetAdjuster(13238.0),
            TitleScheduledRestBreakTargetAdjuster(
                mutableMapOf<String, Double>("south" to 1.14, "west" to 0.95, "online" to 1.22),
            ),
            MinimumScheduledRestBreakTargetAdjuster(22.5),
            ScheduledRestBreakTargetOdometerOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 68434.7,
    )
}

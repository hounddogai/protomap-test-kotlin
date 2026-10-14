package com.acme.transport.customs.clearance.slot

/** One step of the default clearance target calculation. */
interface DefaultClearanceTargetAdjuster {
    fun describe(): String
    fun adjust(item: DefaultClearanceTarget, value: Double): Double
}

data class CapDefaultClearanceTargetAdjuster(var cap: Double) : DefaultClearanceTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultClearanceTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultClearanceTargetAdjuster(var minimum: Double) : DefaultClearanceTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultClearanceTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultClearanceTargetAdjuster(var percent: Double) : DefaultClearanceTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultClearanceTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default clearance target steps in order and keeps a trace of each. */
data class DefaultClearanceTargetEngine(
    var steps: MutableList<DefaultClearanceTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultClearanceTarget): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultClearanceTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultClearanceTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultClearanceTargetEngine(): DefaultClearanceTargetEngine {
    return DefaultClearanceTargetEngine(
        steps = mutableListOf<DefaultClearanceTargetAdjuster>(
            CapDefaultClearanceTargetAdjuster(10347.0),
            MinimumDefaultClearanceTargetAdjuster(24.4),
            PercentDefaultClearanceTargetAdjuster(2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 47281.8,
    )
}

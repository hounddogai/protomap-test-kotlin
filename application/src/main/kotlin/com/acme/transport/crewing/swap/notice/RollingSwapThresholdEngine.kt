package com.acme.transport.crewing.swap.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling swap threshold calculation. */
interface RollingSwapThresholdFactor {
    fun describe(): String
    fun adjust(item: RollingSwapThreshold, value: Double): Double
}

data class RoundingRollingSwapThresholdAdjuster(var step: Double) : RollingSwapThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingSwapThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingSwapThresholdAdjuster(var minimum: Double) : RollingSwapThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingSwapThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingSwapThresholdStartsAtOffsetAdjuster(var weight: Double) : RollingSwapThresholdFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RollingSwapThreshold, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the rolling swap threshold steps in order and keeps a trace of each. */
data class RollingSwapThresholdEngine(
    var steps: MutableList<RollingSwapThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingSwapThreshold): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingSwapThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingSwapThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingSwapThresholdEngine(): RollingSwapThresholdEngine {
    return RollingSwapThresholdEngine(
        steps = mutableListOf<RollingSwapThresholdFactor>(
            RoundingRollingSwapThresholdAdjuster(0.05),
            MinimumRollingSwapThresholdAdjuster(33.8),
            RollingSwapThresholdStartsAtOffsetAdjuster(0.22),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 96646.0,
    )
}

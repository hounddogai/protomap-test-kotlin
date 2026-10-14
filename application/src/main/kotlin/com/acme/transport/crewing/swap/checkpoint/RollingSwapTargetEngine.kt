package com.acme.transport.crewing.swap.checkpoint

/** One step of the rolling swap target calculation. */
interface RollingSwapTargetStep {
    fun describe(): String
    fun adjust(item: RollingSwapTarget, value: Double): Double
}

data class MinimumRollingSwapTargetAdjuster(var minimum: Double) : RollingSwapTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingSwapTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingSwapTargetEtaMinutesOffsetAdjuster(var weight: Double) : RollingSwapTargetStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: RollingSwapTarget, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class TieredRollingSwapTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingSwapTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingSwapTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingSwapTargetAdjuster(var cap: Double) : RollingSwapTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingSwapTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling swap target steps in order and keeps a trace of each. */
data class RollingSwapTargetEngine(
    var steps: MutableList<RollingSwapTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingSwapTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingSwapTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingSwapTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingSwapTargetEngine(): RollingSwapTargetEngine {
    return RollingSwapTargetEngine(
        steps = mutableListOf<RollingSwapTargetStep>(
            MinimumRollingSwapTargetAdjuster(37.6),
            RollingSwapTargetEtaMinutesOffsetAdjuster(0.47),
            TieredRollingSwapTargetAdjuster(
                mutableListOf<Double>(6131.0, 6418.0, 8854.0, 9334.0),
                mutableListOf<Double>(1.04, 0.97, 1.11, 1.15),
            ),
            CapRollingSwapTargetAdjuster(49167.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 49863.3,
    )
}

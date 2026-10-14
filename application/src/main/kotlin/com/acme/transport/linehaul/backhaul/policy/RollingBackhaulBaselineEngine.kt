package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling backhaul baseline calculation. */
interface RollingBackhaulBaselineStep {
    fun describe(): String
    fun adjust(item: RollingBackhaulBaseline, value: Double): Double
}

data class RoundingRollingBackhaulBaselineAdjuster(var step: Double) : RollingBackhaulBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingBackhaulBaseline, value: Double): Double = roundTo(value, step)
}

data class CapRollingBackhaulBaselineAdjuster(var cap: Double) : RollingBackhaulBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingBackhaulBaseline, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingBackhaulBaselineAdjuster(var minimum: Double) : RollingBackhaulBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingBackhaulBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling backhaul baseline steps in order and keeps a trace of each. */
data class RollingBackhaulBaselineEngine(
    var steps: MutableList<RollingBackhaulBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingBackhaulBaseline): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingBackhaulBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingBackhaulBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingBackhaulBaselineEngine(): RollingBackhaulBaselineEngine {
    return RollingBackhaulBaselineEngine(
        steps = mutableListOf<RollingBackhaulBaselineStep>(
            RoundingRollingBackhaulBaselineAdjuster(0.1),
            CapRollingBackhaulBaselineAdjuster(33596.0),
            MinimumRollingBackhaulBaselineAdjuster(33.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 6786.7,
    )
}

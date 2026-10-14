package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling capacity rollup calculation. */
interface RollingCapacityRollupStep {
    fun describe(): String
    fun adjust(item: RollingCapacityRollup, value: Double): Double
}

data class RoundingRollingCapacityRollupAdjuster(var step: Double) : RollingCapacityRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCapacityRollup, value: Double): Double = roundTo(value, step)
}

data class CapRollingCapacityRollupAdjuster(var cap: Double) : RollingCapacityRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCapacityRollup, value: Double): Double = minOf(value, cap)
}

data class TieredRollingCapacityRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCapacityRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCapacityRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling capacity rollup steps in order and keeps a trace of each. */
data class RollingCapacityRollupEngine(
    var steps: MutableList<RollingCapacityRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingCapacityRollup): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingCapacityRollupStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingCapacityRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingCapacityRollupEngine(): RollingCapacityRollupEngine {
    return RollingCapacityRollupEngine(
        steps = mutableListOf<RollingCapacityRollupStep>(
            RoundingRollingCapacityRollupAdjuster(1.0),
            CapRollingCapacityRollupAdjuster(47349.0),
            TieredRollingCapacityRollupAdjuster(
                mutableListOf<Double>(2201.0, 6966.0),
                mutableListOf<Double>(1.15, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 45163.6,
    )
}

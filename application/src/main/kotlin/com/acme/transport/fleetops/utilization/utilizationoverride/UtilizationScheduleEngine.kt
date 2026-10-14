package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.numbers.roundTo

/** One step of the utilization schedule calculation. */
interface UtilizationScheduleStep {
    fun describe(): String
    fun adjust(item: UtilizationSchedule, value: Double): Double
}

data class UtilizationScheduleLimitOffsetAdjuster(var weight: Double) : UtilizationScheduleStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: UtilizationSchedule, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentUtilizationScheduleAdjuster(var percent: Double) : UtilizationScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneUtilizationScheduleAdjuster(var factors: MutableMap<String, Double>) : UtilizationScheduleStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: UtilizationSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredUtilizationScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingUtilizationScheduleAdjuster(var step: Double) : UtilizationScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationSchedule, value: Double): Double = roundTo(value, step)
}

data class CapUtilizationScheduleAdjuster(var cap: Double) : UtilizationScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationSchedule, value: Double): Double = minOf(value, cap)
}

/** Runs the utilization schedule steps in order and keeps a trace of each. */
data class UtilizationScheduleEngine(
    var steps: MutableList<UtilizationScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationSchedule): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationScheduleEngine(): UtilizationScheduleEngine {
    return UtilizationScheduleEngine(
        steps = mutableListOf<UtilizationScheduleStep>(
            UtilizationScheduleLimitOffsetAdjuster(1.16),
            PercentUtilizationScheduleAdjuster(2.1),
            LaneUtilizationScheduleAdjuster(mutableMapOf<String, Double>("central" to 0.95, "south" to 0.73)),
            TieredUtilizationScheduleAdjuster(
                mutableListOf<Double>(778.0, 7818.0, 9222.0, 9347.0),
                mutableListOf<Double>(0.83, 1.02, 1.0, 0.83),
            ),
            RoundingUtilizationScheduleAdjuster(1.0),
            CapUtilizationScheduleAdjuster(35558.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 77674.4,
    )
}

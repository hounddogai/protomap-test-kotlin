package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy dock door metric calculation. */
interface LegacyDockDoorMetricStep {
    fun describe(): String
    fun adjust(item: LegacyDockDoorMetric, value: Double): Double
}

data class RoundingLegacyDockDoorMetricAdjuster(var step: Double) : LegacyDockDoorMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDockDoorMetric, value: Double): Double = roundTo(value, step)
}

data class CapLegacyDockDoorMetricAdjuster(var cap: Double) : LegacyDockDoorMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDockDoorMetric, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyDockDoorMetricAdjuster(var minimum: Double) : LegacyDockDoorMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDockDoorMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyDockDoorMetricAmountOffsetAdjuster(var weight: Double) : LegacyDockDoorMetricStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: LegacyDockDoorMetric, value: Double): Double = value + item.amount * weight
}

/** Runs the legacy dock door metric steps in order and keeps a trace of each. */
data class LegacyDockDoorMetricEngine(
    var steps: MutableList<LegacyDockDoorMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDockDoorMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDockDoorMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDockDoorMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDockDoorMetricEngine(): LegacyDockDoorMetricEngine {
    return LegacyDockDoorMetricEngine(
        steps = mutableListOf<LegacyDockDoorMetricStep>(
            RoundingLegacyDockDoorMetricAdjuster(5.0),
            CapLegacyDockDoorMetricAdjuster(2642.0),
            MinimumLegacyDockDoorMetricAdjuster(9.3),
            LegacyDockDoorMetricAmountOffsetAdjuster(0.92),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 80312.8,
    )
}

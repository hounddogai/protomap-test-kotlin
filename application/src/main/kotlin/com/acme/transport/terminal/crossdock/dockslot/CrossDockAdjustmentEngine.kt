package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock adjustment calculation. */
interface CrossDockAdjustmentStep {
    fun describe(): String
    fun adjust(item: CrossDockAdjustment, value: Double): Double
}

data class CrossDockAdjustmentStopCountOffsetAdjuster(var weight: Double) : CrossDockAdjustmentStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: CrossDockAdjustment, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class PercentCrossDockAdjustmentAdjuster(var percent: Double) : CrossDockAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCrossDockAdjustmentAdjuster(var step: Double) : CrossDockAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockAdjustment, value: Double): Double = roundTo(value, step)
}

data class MinimumCrossDockAdjustmentAdjuster(var minimum: Double) : CrossDockAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCrossDockAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryCrossDockAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CrossDockAdjustmentStep {
    override fun describe(): String = "by country"

    override fun adjust(item: CrossDockAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cross dock adjustment steps in order and keeps a trace of each. */
data class CrossDockAdjustmentEngine(
    var steps: MutableList<CrossDockAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockAdjustment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockAdjustmentEngine(flags: Registry): CrossDockAdjustmentEngine {
    return CrossDockAdjustmentEngine(
        steps = mutableListOf<CrossDockAdjustmentStep>(
            CrossDockAdjustmentStopCountOffsetAdjuster(1.4),
            PercentCrossDockAdjustmentAdjuster(-7.0),
            RoundingCrossDockAdjustmentAdjuster(0.5),
            MinimumCrossDockAdjustmentAdjuster(18.6),
            FlaggedCrossDockAdjustmentAdjuster(flags, "cross-dock-adjustment-regional", 0.9),
            CountryCrossDockAdjustmentAdjuster(mutableMapOf<String, Double>("south" to 1.09, "central" to 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 39482.9,
    )
}

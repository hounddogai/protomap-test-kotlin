package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door exception calculation. */
interface DockDoorExceptionModifier {
    fun describe(): String
    fun adjust(item: DockDoorException, value: Double): Double
}

data class DockDoorExceptionOdometerOffsetAdjuster(var weight: Double) : DockDoorExceptionModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: DockDoorException, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredDockDoorExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryDockDoorExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : DockDoorExceptionModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: DockDoorException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDockDoorExceptionAdjuster(var step: Double) : DockDoorExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorException, value: Double): Double = roundTo(value, step)
}

data class PercentDockDoorExceptionAdjuster(var percent: Double) : DockDoorExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dock door exception steps in order and keeps a trace of each. */
data class DockDoorExceptionEngine(
    var steps: MutableList<DockDoorExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorException): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorExceptionEngine(): DockDoorExceptionEngine {
    return DockDoorExceptionEngine(
        steps = mutableListOf<DockDoorExceptionModifier>(
            DockDoorExceptionOdometerOffsetAdjuster(1.82),
            TieredDockDoorExceptionAdjuster(
                mutableListOf<Double>(133.0, 6946.0),
                mutableListOf<Double>(1.13, 1.03),
            ),
            CategoryDockDoorExceptionAdjuster(
                mutableMapOf<String, Double>("north" to 1.0, "central" to 1.39, "west" to 1.32),
            ),
            RoundingDockDoorExceptionAdjuster(1.0),
            PercentDockDoorExceptionAdjuster(-17.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 32429.4,
    )
}

package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door adjustment calculation. */
interface DockDoorAdjustmentStep {
    fun describe(): String
    fun adjust(item: DockDoorAdjustment, value: Double): Double
}

data class PercentDockDoorAdjustmentAdjuster(var percent: Double) : DockDoorAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleDockDoorAdjustmentAdjuster(var factors: MutableMap<String, Double>) : DockDoorAdjustmentStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DockDoorAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDockDoorAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DockDoorAdjustmentCostOffsetAdjuster(var weight: Double) : DockDoorAdjustmentStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: DockDoorAdjustment, value: Double): Double = value + item.cost * weight
}

data class RoundingDockDoorAdjustmentAdjuster(var step: Double) : DockDoorAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the dock door adjustment steps in order and keeps a trace of each. */
data class DockDoorAdjustmentEngine(
    var steps: MutableList<DockDoorAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorAdjustment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorAdjustmentEngine(flags: Registry): DockDoorAdjustmentEngine {
    return DockDoorAdjustmentEngine(
        steps = mutableListOf<DockDoorAdjustmentStep>(
            PercentDockDoorAdjustmentAdjuster(1.4),
            TitleDockDoorAdjustmentAdjuster(mutableMapOf<String, Double>("east" to 1.01)),
            FlaggedDockDoorAdjustmentAdjuster(flags, "dock-door-adjustment-tiered", -0.5),
            DockDoorAdjustmentCostOffsetAdjuster(0.95),
            RoundingDockDoorAdjustmentAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 18429.9,
    )
}

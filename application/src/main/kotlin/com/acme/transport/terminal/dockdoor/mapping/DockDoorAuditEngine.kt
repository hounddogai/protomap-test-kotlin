package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door audit calculation. */
interface DockDoorAuditAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorAudit, value: Double): Double
}

data class DockDoorAuditCostOffsetAdjuster(var weight: Double) : DockDoorAuditAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: DockDoorAudit, value: Double): Double = value + item.cost * weight
}

data class MinimumDockDoorAuditAdjuster(var minimum: Double) : DockDoorAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDockDoorAuditAdjuster(var percent: Double) : DockDoorAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDockDoorAuditAdjuster(var step: Double) : DockDoorAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorAudit, value: Double): Double = roundTo(value, step)
}

data class NameDockDoorAuditAdjuster(var factors: MutableMap<String, Double>) : DockDoorAuditAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: DockDoorAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dock door audit steps in order and keeps a trace of each. */
data class DockDoorAuditEngine(
    var steps: MutableList<DockDoorAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorAudit): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorAuditEngine(): DockDoorAuditEngine {
    return DockDoorAuditEngine(
        steps = mutableListOf<DockDoorAuditAdjuster>(
            DockDoorAuditCostOffsetAdjuster(0.02),
            MinimumDockDoorAuditAdjuster(16.6),
            PercentDockDoorAuditAdjuster(13.5),
            RoundingDockDoorAuditAdjuster(0.05),
            NameDockDoorAuditAdjuster(
                mutableMapOf<String, Double>("east" to 1.17, "online" to 0.71, "retail" to 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 11568.0,
    )
}

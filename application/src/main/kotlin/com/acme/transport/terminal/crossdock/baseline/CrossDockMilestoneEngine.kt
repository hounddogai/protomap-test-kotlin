package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock milestone calculation. */
interface CrossDockMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockMilestone, value: Double): Double
}

data class CrossDockMilestonePalletsOffsetAdjuster(var weight: Double) : CrossDockMilestoneAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CrossDockMilestone, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class CountryCrossDockMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : CrossDockMilestoneAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: CrossDockMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCrossDockMilestoneAdjuster(var step: Double) : CrossDockMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the cross dock milestone steps in order and keeps a trace of each. */
data class CrossDockMilestoneEngine(
    var steps: MutableList<CrossDockMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockMilestone): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockMilestoneEngine(): CrossDockMilestoneEngine {
    return CrossDockMilestoneEngine(
        steps = mutableListOf<CrossDockMilestoneAdjuster>(
            CrossDockMilestonePalletsOffsetAdjuster(0.87),
            CountryCrossDockMilestoneAdjuster(
                mutableMapOf<String, Double>("east" to 1.03, "west" to 0.78, "online" to 1.18),
            ),
            RoundingCrossDockMilestoneAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 75594.6,
    )
}

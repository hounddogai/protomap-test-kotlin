package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.flags.Registry

/** One step of the primary dock door summary calculation. */
interface PrimaryDockDoorSummaryAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDockDoorSummary, value: Double): Double
}

data class FlaggedPrimaryDockDoorSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDockDoorSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDockDoorSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryDockDoorSummaryAdjuster(var percent: Double) : PrimaryDockDoorSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDockDoorSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryDockDoorSummaryRatioOffsetAdjuster(var weight: Double) : PrimaryDockDoorSummaryAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PrimaryDockDoorSummary, value: Double): Double = value + item.ratio * weight
}

data class MinimumPrimaryDockDoorSummaryAdjuster(var minimum: Double) : PrimaryDockDoorSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDockDoorSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocalePrimaryDockDoorSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDockDoorSummaryAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryDockDoorSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary dock door summary steps in order and keeps a trace of each. */
data class PrimaryDockDoorSummaryEngine(
    var steps: MutableList<PrimaryDockDoorSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryDockDoorSummary): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDockDoorSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDockDoorSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryDockDoorSummaryEngine(flags: Registry): PrimaryDockDoorSummaryEngine {
    return PrimaryDockDoorSummaryEngine(
        steps = mutableListOf<PrimaryDockDoorSummaryAdjuster>(
            FlaggedPrimaryDockDoorSummaryAdjuster(flags, "primary-dock-door-summary-tiered", -9.6),
            PercentPrimaryDockDoorSummaryAdjuster(-19.1),
            PrimaryDockDoorSummaryRatioOffsetAdjuster(1.95),
            MinimumPrimaryDockDoorSummaryAdjuster(36.7),
            LocalePrimaryDockDoorSummaryAdjuster(mutableMapOf<String, Double>("east" to 1.33)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 9836.9,
    )
}

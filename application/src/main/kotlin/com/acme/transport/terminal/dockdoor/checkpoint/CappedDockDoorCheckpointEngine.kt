package com.acme.transport.terminal.dockdoor.checkpoint

/** One step of the capped dock door checkpoint calculation. */
interface CappedDockDoorCheckpointStep {
    fun describe(): String
    fun adjust(item: CappedDockDoorCheckpoint, value: Double): Double
}

data class MinimumCappedDockDoorCheckpointAdjuster(var minimum: Double) : CappedDockDoorCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDockDoorCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedDockDoorCheckpointAdjuster(var cap: Double) : CappedDockDoorCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDockDoorCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredCappedDockDoorCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDockDoorCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDockDoorCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedDockDoorCheckpointAdjuster(var percent: Double) : CappedDockDoorCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDockDoorCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCappedDockDoorCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDockDoorCheckpointStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedDockDoorCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped dock door checkpoint steps in order and keeps a trace of each. */
data class CappedDockDoorCheckpointEngine(
    var steps: MutableList<CappedDockDoorCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedDockDoorCheckpoint): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDockDoorCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDockDoorCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedDockDoorCheckpointEngine(): CappedDockDoorCheckpointEngine {
    return CappedDockDoorCheckpointEngine(
        steps = mutableListOf<CappedDockDoorCheckpointStep>(
            MinimumCappedDockDoorCheckpointAdjuster(48.6),
            CapCappedDockDoorCheckpointAdjuster(9781.0),
            TieredCappedDockDoorCheckpointAdjuster(
                mutableListOf<Double>(2105.0, 5401.0, 7604.0),
                mutableListOf<Double>(0.94, 0.95, 1.05),
            ),
            PercentCappedDockDoorCheckpointAdjuster(-12.4),
            TitleCappedDockDoorCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.02, "east" to 0.74, "west" to 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 78007.4,
    )
}

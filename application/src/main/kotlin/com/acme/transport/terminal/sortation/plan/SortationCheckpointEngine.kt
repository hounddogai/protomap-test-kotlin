package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation checkpoint calculation. */
interface SortationCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: SortationCheckpoint, value: Double): Double
}

data class MinimumSortationCheckpointAdjuster(var minimum: Double) : SortationCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSortationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSortationCheckpointAdjuster(var step: Double) : SortationCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationCheckpoint, value: Double): Double = roundTo(value, step)
}

data class FlaggedSortationCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSortationCheckpointAdjuster(var percent: Double) : SortationCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleSortationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : SortationCheckpointAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: SortationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the sortation checkpoint steps in order and keeps a trace of each. */
data class SortationCheckpointEngine(
    var steps: MutableList<SortationCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SortationCheckpoint): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SortationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSortationCheckpointEngine(flags: Registry): SortationCheckpointEngine {
    return SortationCheckpointEngine(
        steps = mutableListOf<SortationCheckpointAdjuster>(
            MinimumSortationCheckpointAdjuster(13.2),
            TieredSortationCheckpointAdjuster(
                mutableListOf<Double>(2524.0, 5911.0),
                mutableListOf<Double>(1.02, 1.2),
            ),
            RoundingSortationCheckpointAdjuster(0.5),
            FlaggedSortationCheckpointAdjuster(flags, "sortation-checkpoint-capped", 9.4),
            PercentSortationCheckpointAdjuster(21.3),
            LocaleSortationCheckpointAdjuster(mutableMapOf<String, Double>("west" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 43172.9,
    )
}

package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation group calculation. */
interface SortationGroupStep {
    fun describe(): String
    fun adjust(item: SortationGroup, value: Double): Double
}

data class FlaggedSortationGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitSortationGroupAdjuster(var factors: MutableMap<String, Double>) : SortationGroupStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: SortationGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSortationGroupAdjuster(var step: Double) : SortationGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumSortationGroupAdjuster(var minimum: Double) : SortationGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the sortation group steps in order and keeps a trace of each. */
data class SortationGroupEngine(
    var steps: MutableList<SortationGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationGroup): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<SortationGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationGroupEngine(flags: Registry): SortationGroupEngine {
    return SortationGroupEngine(
        steps = mutableListOf<SortationGroupStep>(
            FlaggedSortationGroupAdjuster(flags, "sortation-group-capped", 8.3),
            UnitSortationGroupAdjuster(
                mutableMapOf<String, Double>("online" to 0.93, "south" to 0.83, "central" to 1.03),
            ),
            RoundingSortationGroupAdjuster(0.1),
            MinimumSortationGroupAdjuster(38.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 63344.9,
    )
}

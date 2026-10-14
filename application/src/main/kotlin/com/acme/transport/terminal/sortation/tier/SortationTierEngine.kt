package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation tier calculation. */
interface SortationTierModifier {
    fun describe(): String
    fun adjust(item: SortationTier, value: Double): Double
}

data class MinimumSortationTierAdjuster(var minimum: Double) : SortationTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSortationTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelSortationTierAdjuster(var factors: MutableMap<String, Double>) : SortationTierModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: SortationTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSortationTierAdjuster(var step: Double) : SortationTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationTier, value: Double): Double = roundTo(value, step)
}

data class PercentSortationTierAdjuster(var percent: Double) : SortationTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SortationTierDurationMinutesOffsetAdjuster(var weight: Double) : SortationTierModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: SortationTier, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

/** Runs the sortation tier steps in order and keeps a trace of each. */
data class SortationTierEngine(
    var steps: MutableList<SortationTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SortationTier): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SortationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSortationTierEngine(flags: Registry): SortationTierEngine {
    return SortationTierEngine(
        steps = mutableListOf<SortationTierModifier>(
            MinimumSortationTierAdjuster(44.5),
            FlaggedSortationTierAdjuster(flags, "sortation-tier-rolling", 7.7),
            LabelSortationTierAdjuster(
                mutableMapOf<String, Double>("central" to 0.8, "west" to 0.77, "north" to 0.71),
            ),
            RoundingSortationTierAdjuster(1.0),
            PercentSortationTierAdjuster(3.3),
            SortationTierDurationMinutesOffsetAdjuster(0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 34609.9,
    )
}

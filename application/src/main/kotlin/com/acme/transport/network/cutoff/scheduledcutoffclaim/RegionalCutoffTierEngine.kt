package com.acme.transport.network.cutoff.scheduledcutoffclaim

/** One step of the regional cutoff tier calculation. */
interface RegionalCutoffTierStep {
    fun describe(): String
    fun adjust(item: RegionalCutoffTier, value: Double): Double
}

data class CapRegionalCutoffTierAdjuster(var cap: Double) : RegionalCutoffTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCutoffTier, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalCutoffTierAdjuster(var percent: Double) : RegionalCutoffTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCutoffTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleRegionalCutoffTierAdjuster(var factors: MutableMap<String, Double>) : RegionalCutoffTierStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalCutoffTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalCutoffTierScoreOffsetAdjuster(var weight: Double) : RegionalCutoffTierStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RegionalCutoffTier, value: Double): Double = value + item.score * weight
}

data class MinimumRegionalCutoffTierAdjuster(var minimum: Double) : RegionalCutoffTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCutoffTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional cutoff tier steps in order and keeps a trace of each. */
data class RegionalCutoffTierEngine(
    var steps: MutableList<RegionalCutoffTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalCutoffTier): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCutoffTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCutoffTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalCutoffTierEngine(): RegionalCutoffTierEngine {
    return RegionalCutoffTierEngine(
        steps = mutableListOf<RegionalCutoffTierStep>(
            CapRegionalCutoffTierAdjuster(24875.0),
            PercentRegionalCutoffTierAdjuster(18.1),
            TitleRegionalCutoffTierAdjuster(
                mutableMapOf<String, Double>("retail" to 0.8, "east" to 0.7, "north" to 1.32),
            ),
            RegionalCutoffTierScoreOffsetAdjuster(1.59),
            MinimumRegionalCutoffTierAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 21413.4,
    )
}

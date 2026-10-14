package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the primary sortation milestone calculation. */
interface PrimarySortationMilestoneStep {
    fun describe(): String
    fun adjust(item: PrimarySortationMilestone, value: Double): Double
}

data class PrimarySortationMilestoneRevisionOffsetAdjuster(var weight: Double) : PrimarySortationMilestoneStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PrimarySortationMilestone, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class MinimumPrimarySortationMilestoneAdjuster(var minimum: Double) : PrimarySortationMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimarySortationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitPrimarySortationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimarySortationMilestoneStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: PrimarySortationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPrimarySortationMilestoneAdjuster(var cap: Double) : PrimarySortationMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimarySortationMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimarySortationMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimarySortationMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimarySortationMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimarySortationMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimarySortationMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimarySortationMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary sortation milestone steps in order and keeps a trace of each. */
data class PrimarySortationMilestoneEngine(
    var steps: MutableList<PrimarySortationMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimarySortationMilestone): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimarySortationMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimarySortationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimarySortationMilestoneEngine(flags: Registry): PrimarySortationMilestoneEngine {
    return PrimarySortationMilestoneEngine(
        steps = mutableListOf<PrimarySortationMilestoneStep>(
            PrimarySortationMilestoneRevisionOffsetAdjuster(1.34),
            MinimumPrimarySortationMilestoneAdjuster(48.3),
            UnitPrimarySortationMilestoneAdjuster(mutableMapOf<String, Double>("north" to 1.21)),
            CapPrimarySortationMilestoneAdjuster(19066.0),
            FlaggedPrimarySortationMilestoneAdjuster(flags, "primary-sortation-milestone-batched", -5.5),
            TieredPrimarySortationMilestoneAdjuster(
                mutableListOf<Double>(613.0, 3962.0, 7743.0, 8265.0),
                mutableListOf<Double>(0.92, 0.99, 1.09, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 16309.6,
    )
}

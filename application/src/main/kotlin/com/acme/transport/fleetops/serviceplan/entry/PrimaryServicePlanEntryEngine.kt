package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary service plan entry calculation. */
interface PrimaryServicePlanEntryFactor {
    fun describe(): String
    fun adjust(item: PrimaryServicePlanEntry, value: Double): Double
}

data class PrimaryServicePlanEntryEndsAtOffsetAdjuster(var weight: Double) : PrimaryServicePlanEntryFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: PrimaryServicePlanEntry, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class CapPrimaryServicePlanEntryAdjuster(var cap: Double) : PrimaryServicePlanEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServicePlanEntry, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryServicePlanEntryAdjuster(var percent: Double) : PrimaryServicePlanEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryServicePlanEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryServicePlanEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServicePlanEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServicePlanEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryServicePlanEntryAdjuster(var step: Double) : PrimaryServicePlanEntryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryServicePlanEntry, value: Double): Double = roundTo(value, step)
}

data class ChannelPrimaryServicePlanEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryServicePlanEntryFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: PrimaryServicePlanEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary service plan entry steps in order and keeps a trace of each. */
data class PrimaryServicePlanEntryEngine(
    var steps: MutableList<PrimaryServicePlanEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryServicePlanEntry): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryServicePlanEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryServicePlanEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryServicePlanEntryEngine(): PrimaryServicePlanEntryEngine {
    return PrimaryServicePlanEntryEngine(
        steps = mutableListOf<PrimaryServicePlanEntryFactor>(
            PrimaryServicePlanEntryEndsAtOffsetAdjuster(1.07),
            CapPrimaryServicePlanEntryAdjuster(7492.0),
            PercentPrimaryServicePlanEntryAdjuster(-23.1),
            TieredPrimaryServicePlanEntryAdjuster(
                mutableListOf<Double>(5449.0, 6584.0),
                mutableListOf<Double>(1.03, 1.02),
            ),
            RoundingPrimaryServicePlanEntryAdjuster(5.0),
            ChannelPrimaryServicePlanEntryAdjuster(
                mutableMapOf<String, Double>("central" to 1.36, "east" to 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 77604.8,
    )
}

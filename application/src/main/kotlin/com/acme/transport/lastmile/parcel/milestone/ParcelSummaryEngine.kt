package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the parcel summary calculation. */
interface ParcelSummaryModifier {
    fun describe(): String
    fun adjust(item: ParcelSummary, value: Double): Double
}

data class FlaggedParcelSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapParcelSummaryAdjuster(var cap: Double) : ParcelSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumParcelSummaryAdjuster(var minimum: Double) : ParcelSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentParcelSummaryAdjuster(var percent: Double) : ParcelSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneParcelSummaryAdjuster(var factors: MutableMap<String, Double>) : ParcelSummaryModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ParcelSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the parcel summary steps in order and keeps a trace of each. */
data class ParcelSummaryEngine(
    var steps: MutableList<ParcelSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ParcelSummary): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultParcelSummaryEngine(flags: Registry): ParcelSummaryEngine {
    return ParcelSummaryEngine(
        steps = mutableListOf<ParcelSummaryModifier>(
            FlaggedParcelSummaryAdjuster(flags, "parcel-summary-seasonal", -2.7),
            CapParcelSummaryAdjuster(6428.0),
            MinimumParcelSummaryAdjuster(28.7),
            PercentParcelSummaryAdjuster(-17.7),
            LaneParcelSummaryAdjuster(mutableMapOf<String, Double>("central" to 0.88, "west" to 0.72)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 62109.8,
    )
}

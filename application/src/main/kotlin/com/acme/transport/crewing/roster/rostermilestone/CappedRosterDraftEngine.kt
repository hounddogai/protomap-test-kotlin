package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped roster draft calculation. */
interface CappedRosterDraftFactor {
    fun describe(): String
    fun adjust(item: CappedRosterDraft, value: Double): Double
}

data class RoundingCappedRosterDraftAdjuster(var step: Double) : CappedRosterDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRosterDraft, value: Double): Double = roundTo(value, step)
}

data class LabelCappedRosterDraftAdjuster(var factors: MutableMap<String, Double>) : CappedRosterDraftFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedRosterDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedRosterDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRosterDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRosterDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedRosterDraftRatioOffsetAdjuster(var weight: Double) : CappedRosterDraftFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: CappedRosterDraft, value: Double): Double = value + item.ratio * weight
}

data class MinimumCappedRosterDraftAdjuster(var minimum: Double) : CappedRosterDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRosterDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedRosterDraftAdjuster(var cap: Double) : CappedRosterDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRosterDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the capped roster draft steps in order and keeps a trace of each. */
data class CappedRosterDraftEngine(
    var steps: MutableList<CappedRosterDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRosterDraft): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRosterDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRosterDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRosterDraftEngine(): CappedRosterDraftEngine {
    return CappedRosterDraftEngine(
        steps = mutableListOf<CappedRosterDraftFactor>(
            RoundingCappedRosterDraftAdjuster(0.25),
            LabelCappedRosterDraftAdjuster(mutableMapOf<String, Double>("central" to 0.89)),
            TieredCappedRosterDraftAdjuster(
                mutableListOf<Double>(3116.0, 9627.0),
                mutableListOf<Double>(0.92, 0.81),
            ),
            CappedRosterDraftRatioOffsetAdjuster(1.37),
            MinimumCappedRosterDraftAdjuster(43.6),
            CapCappedRosterDraftAdjuster(5551.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 50916.5,
    )
}

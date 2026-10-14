package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted roster summary calculation. */
interface WeightedRosterSummaryFactor {
    fun describe(): String
    fun adjust(item: WeightedRosterSummary, value: Double): Double
}

data class WeightedRosterSummaryGraceDaysOffsetAdjuster(var weight: Double) : WeightedRosterSummaryFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: WeightedRosterSummary, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class MinimumWeightedRosterSummaryAdjuster(var minimum: Double) : WeightedRosterSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRosterSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedRosterSummaryAdjuster(var step: Double) : WeightedRosterSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRosterSummary, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedRosterSummaryAdjuster(var percent: Double) : WeightedRosterSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRosterSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentWeightedRosterSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedRosterSummaryFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: WeightedRosterSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedRosterSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRosterSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRosterSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted roster summary steps in order and keeps a trace of each. */
data class WeightedRosterSummaryEngine(
    var steps: MutableList<WeightedRosterSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedRosterSummary): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRosterSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedRosterSummaryEngine(): WeightedRosterSummaryEngine {
    return WeightedRosterSummaryEngine(
        steps = mutableListOf<WeightedRosterSummaryFactor>(
            WeightedRosterSummaryGraceDaysOffsetAdjuster(0.51),
            MinimumWeightedRosterSummaryAdjuster(15.0),
            RoundingWeightedRosterSummaryAdjuster(0.25),
            PercentWeightedRosterSummaryAdjuster(23.4),
            SegmentWeightedRosterSummaryAdjuster(
                mutableMapOf<String, Double>("online" to 1.34, "south" to 1.2, "central" to 1.26),
            ),
            TieredWeightedRosterSummaryAdjuster(
                mutableListOf<Double>(1331.0, 5635.0, 6157.0, 8764.0),
                mutableListOf<Double>(1.02, 1.17, 1.12, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 6873.7,
    )
}

package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy trailer yard claim calculation. */
interface LegacyTrailerYardClaimAdjuster {
    fun describe(): String
    fun adjust(item: LegacyTrailerYardClaim, value: Double): Double
}

data class NameLegacyTrailerYardClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTrailerYardClaimAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyTrailerYardClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyTrailerYardClaimAdjuster(var step: Double) : LegacyTrailerYardClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTrailerYardClaim, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyTrailerYardClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTrailerYardClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTrailerYardClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyTrailerYardClaimLoadKgOffsetAdjuster(var weight: Double) : LegacyTrailerYardClaimAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LegacyTrailerYardClaim, value: Double): Double = value + item.loadKg * weight
}

data class PercentLegacyTrailerYardClaimAdjuster(var percent: Double) : LegacyTrailerYardClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTrailerYardClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy trailer yard claim steps in order and keeps a trace of each. */
data class LegacyTrailerYardClaimEngine(
    var steps: MutableList<LegacyTrailerYardClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyTrailerYardClaim): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTrailerYardClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTrailerYardClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyTrailerYardClaimEngine(): LegacyTrailerYardClaimEngine {
    return LegacyTrailerYardClaimEngine(
        steps = mutableListOf<LegacyTrailerYardClaimAdjuster>(
            NameLegacyTrailerYardClaimAdjuster(mutableMapOf<String, Double>("central" to 0.95, "retail" to 1.1)),
            RoundingLegacyTrailerYardClaimAdjuster(1.0),
            TieredLegacyTrailerYardClaimAdjuster(
                mutableListOf<Double>(1806.0, 8814.0),
                mutableListOf<Double>(1.19, 1.15),
            ),
            LegacyTrailerYardClaimLoadKgOffsetAdjuster(0.88),
            PercentLegacyTrailerYardClaimAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 24375.7,
    )
}

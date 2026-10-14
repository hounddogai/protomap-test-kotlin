package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the legacy scorecard variance calculation. */
interface LegacyScorecardVarianceStep {
    fun describe(): String
    fun adjust(item: LegacyScorecardVariance, value: Double): Double
}

data class NameLegacyScorecardVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyScorecardVarianceStep {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyScorecardVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyScorecardVarianceAdjuster(var minimum: Double) : LegacyScorecardVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyScorecardVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLegacyScorecardVarianceAdjuster(var cap: Double) : LegacyScorecardVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyScorecardVariance, value: Double): Double = minOf(value, cap)
}

data class LegacyScorecardVarianceMaximumOffsetAdjuster(var weight: Double) : LegacyScorecardVarianceStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LegacyScorecardVariance, value: Double): Double = value + item.maximum * weight
}

data class TieredLegacyScorecardVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyScorecardVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyScorecardVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLegacyScorecardVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyScorecardVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyScorecardVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy scorecard variance steps in order and keeps a trace of each. */
data class LegacyScorecardVarianceEngine(
    var steps: MutableList<LegacyScorecardVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyScorecardVariance): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyScorecardVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyScorecardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyScorecardVarianceEngine(flags: Registry): LegacyScorecardVarianceEngine {
    return LegacyScorecardVarianceEngine(
        steps = mutableListOf<LegacyScorecardVarianceStep>(
            NameLegacyScorecardVarianceAdjuster(mutableMapOf<String, Double>("east" to 1.13, "retail" to 0.94)),
            MinimumLegacyScorecardVarianceAdjuster(8.7),
            CapLegacyScorecardVarianceAdjuster(47382.0),
            LegacyScorecardVarianceMaximumOffsetAdjuster(0.89),
            TieredLegacyScorecardVarianceAdjuster(
                mutableListOf<Double>(131.0, 4605.0, 4634.0),
                mutableListOf<Double>(1.04, 0.89, 1.04),
            ),
            FlaggedLegacyScorecardVarianceAdjuster(flags, "legacy-scorecard-variance-capped", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 54938.1,
    )
}

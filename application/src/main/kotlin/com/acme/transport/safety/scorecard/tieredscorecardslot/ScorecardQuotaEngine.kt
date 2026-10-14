package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard quota calculation. */
interface ScorecardQuotaAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardQuota, value: Double): Double
}

data class TieredScorecardQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelScorecardQuotaAdjuster(var factors: MutableMap<String, Double>) : ScorecardQuotaAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScorecardQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScorecardQuotaExpiresAtOffsetAdjuster(var weight: Double) : ScorecardQuotaAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ScorecardQuota, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingScorecardQuotaAdjuster(var step: Double) : ScorecardQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumScorecardQuotaAdjuster(var minimum: Double) : ScorecardQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScorecardQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scorecard quota steps in order and keeps a trace of each. */
data class ScorecardQuotaEngine(
    var steps: MutableList<ScorecardQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardQuotaEngine(flags: Registry): ScorecardQuotaEngine {
    return ScorecardQuotaEngine(
        steps = mutableListOf<ScorecardQuotaAdjuster>(
            TieredScorecardQuotaAdjuster(
                mutableListOf<Double>(2681.0, 3594.0, 4573.0, 7558.0),
                mutableListOf<Double>(0.95, 0.86, 0.99, 0.84),
            ),
            LabelScorecardQuotaAdjuster(mutableMapOf<String, Double>("retail" to 1.26)),
            ScorecardQuotaExpiresAtOffsetAdjuster(1.77),
            RoundingScorecardQuotaAdjuster(0.5),
            MinimumScorecardQuotaAdjuster(9.6),
            FlaggedScorecardQuotaAdjuster(flags, "scorecard-quota-adaptive", -9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 69355.1,
    )
}

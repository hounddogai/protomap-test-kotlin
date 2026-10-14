package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.flags.Registry

/** One step of the tiered scorecard quota calculation. */
interface TieredScorecardQuotaFactor {
    fun describe(): String
    fun adjust(item: TieredScorecardQuota, value: Double): Double
}

data class FlaggedTieredScorecardQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredScorecardQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredScorecardQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredScorecardQuotaAdjuster(var minimum: Double) : TieredScorecardQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredScorecardQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredScorecardQuotaAdjuster(var cap: Double) : TieredScorecardQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredScorecardQuota, value: Double): Double = minOf(value, cap)
}

data class TieredTieredScorecardQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredScorecardQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredScorecardQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredScorecardQuotaDiscountOffsetAdjuster(var weight: Double) : TieredScorecardQuotaFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: TieredScorecardQuota, value: Double): Double = value + item.discount * weight
}

/** Runs the tiered scorecard quota steps in order and keeps a trace of each. */
data class TieredScorecardQuotaEngine(
    var steps: MutableList<TieredScorecardQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredScorecardQuota): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredScorecardQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredScorecardQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredScorecardQuotaEngine(flags: Registry): TieredScorecardQuotaEngine {
    return TieredScorecardQuotaEngine(
        steps = mutableListOf<TieredScorecardQuotaFactor>(
            FlaggedTieredScorecardQuotaAdjuster(flags, "tiered-scorecard-quota-capped", 6.3),
            MinimumTieredScorecardQuotaAdjuster(11.9),
            CapTieredScorecardQuotaAdjuster(38514.0),
            TieredTieredScorecardQuotaAdjuster(
                mutableListOf<Double>(480.0, 605.0, 5416.0, 6803.0),
                mutableListOf<Double>(0.83, 1.1, 1.06, 1.15),
            ),
            TieredScorecardQuotaDiscountOffsetAdjuster(0.13),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 19650.4,
    )
}

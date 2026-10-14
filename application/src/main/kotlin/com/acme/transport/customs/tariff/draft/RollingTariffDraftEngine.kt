package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.flags.Registry

/** One step of the rolling tariff draft calculation. */
interface RollingTariffDraftAdjuster {
    fun describe(): String
    fun adjust(item: RollingTariffDraft, value: Double): Double
}

data class RegionRollingTariffDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTariffDraftAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: RollingTariffDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingTariffDraftRateOffsetAdjuster(var weight: Double) : RollingTariffDraftAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RollingTariffDraft, value: Double): Double = value + item.rate * weight
}

data class CapRollingTariffDraftAdjuster(var cap: Double) : RollingTariffDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTariffDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingTariffDraftAdjuster(var minimum: Double) : RollingTariffDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTariffDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingTariffDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTariffDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTariffDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingTariffDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTariffDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTariffDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling tariff draft steps in order and keeps a trace of each. */
data class RollingTariffDraftEngine(
    var steps: MutableList<RollingTariffDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingTariffDraft): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTariffDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTariffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingTariffDraftEngine(flags: Registry): RollingTariffDraftEngine {
    return RollingTariffDraftEngine(
        steps = mutableListOf<RollingTariffDraftAdjuster>(
            RegionRollingTariffDraftAdjuster(mutableMapOf<String, Double>("west" to 1.16, "east" to 0.9)),
            RollingTariffDraftRateOffsetAdjuster(0.24),
            CapRollingTariffDraftAdjuster(1393.0),
            MinimumRollingTariffDraftAdjuster(6.5),
            FlaggedRollingTariffDraftAdjuster(flags, "rolling-tariff-draft-primary", -5.5),
            TieredRollingTariffDraftAdjuster(
                mutableListOf<Double>(1442.0, 4360.0, 6519.0, 8092.0),
                mutableListOf<Double>(0.85, 1.03, 1.12, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 50884.3,
    )
}

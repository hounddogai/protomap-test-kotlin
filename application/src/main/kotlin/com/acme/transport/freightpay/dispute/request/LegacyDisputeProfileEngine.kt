package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.flags.Registry

/** One step of the legacy dispute profile calculation. */
interface LegacyDisputeProfileModifier {
    fun describe(): String
    fun adjust(item: LegacyDisputeProfile, value: Double): Double
}

data class LegacyDisputeProfileThresholdOffsetAdjuster(var weight: Double) : LegacyDisputeProfileModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: LegacyDisputeProfile, value: Double): Double = value + item.threshold * weight
}

data class FlaggedLegacyDisputeProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyDisputeProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyDisputeProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelLegacyDisputeProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDisputeProfileModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: LegacyDisputeProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyDisputeProfileAdjuster(var minimum: Double) : LegacyDisputeProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDisputeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyDisputeProfileAdjuster(var percent: Double) : LegacyDisputeProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDisputeProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyDisputeProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDisputeProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDisputeProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy dispute profile steps in order and keeps a trace of each. */
data class LegacyDisputeProfileEngine(
    var steps: MutableList<LegacyDisputeProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyDisputeProfile): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDisputeProfileModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDisputeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyDisputeProfileEngine(flags: Registry): LegacyDisputeProfileEngine {
    return LegacyDisputeProfileEngine(
        steps = mutableListOf<LegacyDisputeProfileModifier>(
            LegacyDisputeProfileThresholdOffsetAdjuster(0.43),
            FlaggedLegacyDisputeProfileAdjuster(flags, "legacy-dispute-profile-legacy", 0.2),
            ChannelLegacyDisputeProfileAdjuster(mutableMapOf<String, Double>("west" to 1.06, "north" to 1.39)),
            MinimumLegacyDisputeProfileAdjuster(44.2),
            PercentLegacyDisputeProfileAdjuster(11.9),
            TieredLegacyDisputeProfileAdjuster(
                mutableListOf<Double>(224.0, 2228.0, 5810.0, 8792.0),
                mutableListOf<Double>(1.05, 0.93, 0.87, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 87043.1,
    )
}

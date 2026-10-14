package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.flags.Registry

/** One step of the legacy relay notice calculation. */
interface LegacyRelayNoticeAdjuster {
    fun describe(): String
    fun adjust(item: LegacyRelayNotice, value: Double): Double
}

data class LocaleLegacyRelayNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyRelayNoticeAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: LegacyRelayNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyRelayNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyRelayNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyRelayNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyRelayNoticeAdjuster(var minimum: Double) : LegacyRelayNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRelayNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLegacyRelayNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRelayNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRelayNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy relay notice steps in order and keeps a trace of each. */
data class LegacyRelayNoticeEngine(
    var steps: MutableList<LegacyRelayNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyRelayNotice): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRelayNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRelayNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyRelayNoticeEngine(flags: Registry): LegacyRelayNoticeEngine {
    return LegacyRelayNoticeEngine(
        steps = mutableListOf<LegacyRelayNoticeAdjuster>(
            LocaleLegacyRelayNoticeAdjuster(mutableMapOf<String, Double>("central" to 1.03, "retail" to 0.72)),
            TieredLegacyRelayNoticeAdjuster(
                mutableListOf<Double>(912.0, 2101.0, 2335.0, 7483.0),
                mutableListOf<Double>(1.15, 0.99, 0.89, 0.92),
            ),
            MinimumLegacyRelayNoticeAdjuster(10.1),
            FlaggedLegacyRelayNoticeAdjuster(flags, "legacy-relay-notice-batched", 1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 51841.0,
    )
}

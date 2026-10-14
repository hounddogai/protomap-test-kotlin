package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.flags.Registry

/** One step of the legacy cross dock variance calculation. */
interface LegacyCrossDockVarianceModifier {
    fun describe(): String
    fun adjust(item: LegacyCrossDockVariance, value: Double): Double
}

data class TieredLegacyCrossDockVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyCrossDockVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyCrossDockVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyCrossDockVarianceAdjuster(var cap: Double) : LegacyCrossDockVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyCrossDockVariance, value: Double): Double = minOf(value, cap)
}

data class RegionLegacyCrossDockVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCrossDockVarianceModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: LegacyCrossDockVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyCrossDockVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCrossDockVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCrossDockVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy cross dock variance steps in order and keeps a trace of each. */
data class LegacyCrossDockVarianceEngine(
    var steps: MutableList<LegacyCrossDockVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyCrossDockVariance): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCrossDockVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCrossDockVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyCrossDockVarianceEngine(flags: Registry): LegacyCrossDockVarianceEngine {
    return LegacyCrossDockVarianceEngine(
        steps = mutableListOf<LegacyCrossDockVarianceModifier>(
            TieredLegacyCrossDockVarianceAdjuster(
                mutableListOf<Double>(1172.0, 2081.0),
                mutableListOf<Double>(1.13, 0.96),
            ),
            CapLegacyCrossDockVarianceAdjuster(11190.0),
            RegionLegacyCrossDockVarianceAdjuster(mutableMapOf<String, Double>("online" to 1.16)),
            FlaggedLegacyCrossDockVarianceAdjuster(flags, "legacy-cross-dock-variance-fallback", 6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 50988.8,
    )
}

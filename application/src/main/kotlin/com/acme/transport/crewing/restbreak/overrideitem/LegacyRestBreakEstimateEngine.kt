package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the legacy rest break estimate calculation. */
interface LegacyRestBreakEstimateModifier {
    fun describe(): String
    fun adjust(item: LegacyRestBreakEstimate, value: Double): Double
}

data class LegacyRestBreakEstimateLoadKgOffsetAdjuster(var weight: Double) : LegacyRestBreakEstimateModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LegacyRestBreakEstimate, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedLegacyRestBreakEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRestBreakEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRestBreakEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyRestBreakEstimateAdjuster(var minimum: Double) : LegacyRestBreakEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRestBreakEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLegacyRestBreakEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyRestBreakEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyRestBreakEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy rest break estimate steps in order and keeps a trace of each. */
data class LegacyRestBreakEstimateEngine(
    var steps: MutableList<LegacyRestBreakEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyRestBreakEstimate): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRestBreakEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRestBreakEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyRestBreakEstimateEngine(flags: Registry): LegacyRestBreakEstimateEngine {
    return LegacyRestBreakEstimateEngine(
        steps = mutableListOf<LegacyRestBreakEstimateModifier>(
            LegacyRestBreakEstimateLoadKgOffsetAdjuster(0.08),
            FlaggedLegacyRestBreakEstimateAdjuster(flags, "legacy-rest-break-estimate-scheduled", 6.6),
            MinimumLegacyRestBreakEstimateAdjuster(6.3),
            TieredLegacyRestBreakEstimateAdjuster(
                mutableListOf<Double>(1838.0, 4264.0, 5767.0, 7704.0),
                mutableListOf<Double>(0.93, 0.94, 0.8, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 83273.0,
    )
}

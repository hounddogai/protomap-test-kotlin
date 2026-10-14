package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy tender variance calculation. */
interface LegacyTenderVarianceFactor {
    fun describe(): String
    fun adjust(item: LegacyTenderVariance, value: Double): Double
}

data class FlaggedLegacyTenderVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTenderVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTenderVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LegacyTenderVarianceAttemptsOffsetAdjuster(var weight: Double) : LegacyTenderVarianceFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: LegacyTenderVariance, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class RoundingLegacyTenderVarianceAdjuster(var step: Double) : LegacyTenderVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTenderVariance, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyTenderVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTenderVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTenderVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelLegacyTenderVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTenderVarianceFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyTenderVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyTenderVarianceAdjuster(var cap: Double) : LegacyTenderVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTenderVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy tender variance steps in order and keeps a trace of each. */
data class LegacyTenderVarianceEngine(
    var steps: MutableList<LegacyTenderVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTenderVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTenderVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTenderVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTenderVarianceEngine(flags: Registry): LegacyTenderVarianceEngine {
    return LegacyTenderVarianceEngine(
        steps = mutableListOf<LegacyTenderVarianceFactor>(
            FlaggedLegacyTenderVarianceAdjuster(flags, "legacy-tender-variance-tiered", 7.3),
            LegacyTenderVarianceAttemptsOffsetAdjuster(1.87),
            RoundingLegacyTenderVarianceAdjuster(0.5),
            TieredLegacyTenderVarianceAdjuster(
                mutableListOf<Double>(5981.0, 7339.0, 8907.0),
                mutableListOf<Double>(1.09, 0.86, 1.12),
            ),
            LabelLegacyTenderVarianceAdjuster(
                mutableMapOf<String, Double>("north" to 1.36, "east" to 1.0, "central" to 0.93),
            ),
            CapLegacyTenderVarianceAdjuster(36249.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 24919.0,
    )
}

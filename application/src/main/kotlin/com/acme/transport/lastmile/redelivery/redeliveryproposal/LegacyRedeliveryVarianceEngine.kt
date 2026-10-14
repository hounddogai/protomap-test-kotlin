package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy redelivery variance calculation. */
interface LegacyRedeliveryVarianceAdjuster {
    fun describe(): String
    fun adjust(item: LegacyRedeliveryVariance, value: Double): Double
}

data class FlaggedLegacyRedeliveryVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRedeliveryVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRedeliveryVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyRedeliveryVarianceAdjuster(var step: Double) : LegacyRedeliveryVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRedeliveryVariance, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyRedeliveryVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyRedeliveryVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyRedeliveryVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyRedeliveryVarianceAdjuster(var cap: Double) : LegacyRedeliveryVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyRedeliveryVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyRedeliveryVarianceAdjuster(var minimum: Double) : LegacyRedeliveryVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRedeliveryVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy redelivery variance steps in order and keeps a trace of each. */
data class LegacyRedeliveryVarianceEngine(
    var steps: MutableList<LegacyRedeliveryVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyRedeliveryVariance): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyRedeliveryVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyRedeliveryVarianceEngine(flags: Registry): LegacyRedeliveryVarianceEngine {
    return LegacyRedeliveryVarianceEngine(
        steps = mutableListOf<LegacyRedeliveryVarianceAdjuster>(
            FlaggedLegacyRedeliveryVarianceAdjuster(flags, "legacy-redelivery-variance-scheduled", 4.5),
            RoundingLegacyRedeliveryVarianceAdjuster(0.5),
            TieredLegacyRedeliveryVarianceAdjuster(
                mutableListOf<Double>(3295.0, 3376.0),
                mutableListOf<Double>(0.82, 1.13),
            ),
            CapLegacyRedeliveryVarianceAdjuster(42680.0),
            MinimumLegacyRedeliveryVarianceAdjuster(48.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 64815.7,
    )
}

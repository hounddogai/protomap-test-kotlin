package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback ev charge entry calculation. */
interface FallbackEvChargeEntryAdjuster {
    fun describe(): String
    fun adjust(item: FallbackEvChargeEntry, value: Double): Double
}

data class TieredFallbackEvChargeEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEvChargeEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEvChargeEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackEvChargeEntryAdjuster(var step: Double) : FallbackEvChargeEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackEvChargeEntry, value: Double): Double = roundTo(value, step)
}

data class CapFallbackEvChargeEntryAdjuster(var cap: Double) : FallbackEvChargeEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackEvChargeEntry, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackEvChargeEntryAdjuster(var percent: Double) : FallbackEvChargeEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackEvChargeEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackEvChargeEntryAdjuster(var minimum: Double) : FallbackEvChargeEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEvChargeEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackEvChargeEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackEvChargeEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackEvChargeEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback ev charge entry steps in order and keeps a trace of each. */
data class FallbackEvChargeEntryEngine(
    var steps: MutableList<FallbackEvChargeEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackEvChargeEntry): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackEvChargeEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackEvChargeEntryEngine(flags: Registry): FallbackEvChargeEntryEngine {
    return FallbackEvChargeEntryEngine(
        steps = mutableListOf<FallbackEvChargeEntryAdjuster>(
            TieredFallbackEvChargeEntryAdjuster(
                mutableListOf<Double>(2086.0, 3171.0, 5700.0, 8667.0),
                mutableListOf<Double>(1.12, 0.91, 0.85, 0.96),
            ),
            RoundingFallbackEvChargeEntryAdjuster(0.1),
            CapFallbackEvChargeEntryAdjuster(49399.0),
            PercentFallbackEvChargeEntryAdjuster(3.3),
            MinimumFallbackEvChargeEntryAdjuster(9.2),
            FlaggedFallbackEvChargeEntryAdjuster(flags, "fallback-ev-charge-entry-scheduled", -8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 20180.5,
    )
}

package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered incident baseline calculation. */
interface TieredIncidentBaselineFactor {
    fun describe(): String
    fun adjust(item: TieredIncidentBaseline, value: Double): Double
}

data class FlaggedTieredIncidentBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredIncidentBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredIncidentBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredIncidentBaselineAdjuster(var minimum: Double) : TieredIncidentBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredIncidentBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredIncidentBaselineEtaMinutesOffsetAdjuster(var weight: Double) : TieredIncidentBaselineFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TieredIncidentBaseline, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class TieredTieredIncidentBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredIncidentBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredIncidentBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredIncidentBaselineAdjuster(var cap: Double) : TieredIncidentBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredIncidentBaseline, value: Double): Double = minOf(value, cap)
}

data class PercentTieredIncidentBaselineAdjuster(var percent: Double) : TieredIncidentBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredIncidentBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered incident baseline steps in order and keeps a trace of each. */
data class TieredIncidentBaselineEngine(
    var steps: MutableList<TieredIncidentBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredIncidentBaseline): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredIncidentBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredIncidentBaselineEngine(flags: Registry): TieredIncidentBaselineEngine {
    return TieredIncidentBaselineEngine(
        steps = mutableListOf<TieredIncidentBaselineFactor>(
            FlaggedTieredIncidentBaselineAdjuster(flags, "tiered-incident-baseline-default", 7.8),
            MinimumTieredIncidentBaselineAdjuster(4.6),
            TieredIncidentBaselineEtaMinutesOffsetAdjuster(1.31),
            TieredTieredIncidentBaselineAdjuster(
                mutableListOf<Double>(1793.0, 2251.0, 5988.0, 7661.0),
                mutableListOf<Double>(0.93, 1.2, 0.8, 0.87),
            ),
            CapTieredIncidentBaselineAdjuster(42899.0),
            PercentTieredIncidentBaselineAdjuster(20.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 19526.1,
    )
}

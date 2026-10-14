package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional broker summary calculation. */
interface RegionalBrokerSummaryFactor {
    fun describe(): String
    fun adjust(item: RegionalBrokerSummary, value: Double): Double
}

data class FlaggedRegionalBrokerSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalBrokerSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalBrokerSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalBrokerSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalBrokerSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalBrokerSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalBrokerSummaryAdjuster(var cap: Double) : RegionalBrokerSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalBrokerSummary, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalBrokerSummaryAdjuster(var percent: Double) : RegionalBrokerSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalBrokerSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitRegionalBrokerSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalBrokerSummaryFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalBrokerSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalBrokerSummaryDurationMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalBrokerSummaryFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RegionalBrokerSummary, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the regional broker summary steps in order and keeps a trace of each. */
data class RegionalBrokerSummaryEngine(
    var steps: MutableList<RegionalBrokerSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalBrokerSummary): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalBrokerSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalBrokerSummaryEngine(flags: Registry): RegionalBrokerSummaryEngine {
    return RegionalBrokerSummaryEngine(
        steps = mutableListOf<RegionalBrokerSummaryFactor>(
            FlaggedRegionalBrokerSummaryAdjuster(flags, "regional-broker-summary-strict", 1.8),
            TieredRegionalBrokerSummaryAdjuster(
                mutableListOf<Double>(3473.0, 6313.0, 6828.0, 7159.0),
                mutableListOf<Double>(0.84, 0.97, 1.19, 0.85),
            ),
            CapRegionalBrokerSummaryAdjuster(28454.0),
            PercentRegionalBrokerSummaryAdjuster(22.9),
            UnitRegionalBrokerSummaryAdjuster(
                mutableMapOf<String, Double>("south" to 1.21, "online" to 1.02, "retail" to 0.76),
            ),
            RegionalBrokerSummaryDurationMinutesOffsetAdjuster(0.56),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 28644.4,
    )
}

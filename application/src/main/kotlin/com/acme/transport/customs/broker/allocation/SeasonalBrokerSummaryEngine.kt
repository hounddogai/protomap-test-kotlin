package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal broker summary calculation. */
interface SeasonalBrokerSummaryModifier {
    fun describe(): String
    fun adjust(item: SeasonalBrokerSummary, value: Double): Double
}

data class FlaggedSeasonalBrokerSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalBrokerSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalBrokerSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalBrokerSummaryAdjuster(var cap: Double) : SeasonalBrokerSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalBrokerSummary, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalBrokerSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalBrokerSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalBrokerSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal broker summary steps in order and keeps a trace of each. */
data class SeasonalBrokerSummaryEngine(
    var steps: MutableList<SeasonalBrokerSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalBrokerSummary): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalBrokerSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalBrokerSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalBrokerSummaryEngine(flags: Registry): SeasonalBrokerSummaryEngine {
    return SeasonalBrokerSummaryEngine(
        steps = mutableListOf<SeasonalBrokerSummaryModifier>(
            FlaggedSeasonalBrokerSummaryAdjuster(flags, "seasonal-broker-summary-rolling", 6.6),
            CapSeasonalBrokerSummaryAdjuster(15322.0),
            TieredSeasonalBrokerSummaryAdjuster(
                mutableListOf<Double>(589.0, 3494.0, 3803.0),
                mutableListOf<Double>(1.04, 1.12, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 56530.3,
    )
}

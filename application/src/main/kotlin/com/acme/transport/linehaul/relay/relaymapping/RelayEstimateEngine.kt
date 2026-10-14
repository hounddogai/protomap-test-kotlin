package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.flags.Registry

/** One step of the relay estimate calculation. */
interface RelayEstimateFactor {
    fun describe(): String
    fun adjust(item: RelayEstimate, value: Double): Double
}

data class TieredRelayEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRelayEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryRelayEstimateAdjuster(var factors: MutableMap<String, Double>) : RelayEstimateFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: RelayEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the relay estimate steps in order and keeps a trace of each. */
data class RelayEstimateEngine(
    var steps: MutableList<RelayEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RelayEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayEstimateEngine(flags: Registry): RelayEstimateEngine {
    return RelayEstimateEngine(
        steps = mutableListOf<RelayEstimateFactor>(
            TieredRelayEstimateAdjuster(
                mutableListOf<Double>(285.0, 2323.0, 6889.0, 9850.0),
                mutableListOf<Double>(1.07, 1.08, 0.97, 0.87),
            ),
            FlaggedRelayEstimateAdjuster(flags, "relay-estimate-adaptive", -5.8),
            CountryRelayEstimateAdjuster(
                mutableMapOf<String, Double>("retail" to 1.11, "central" to 1.11, "west" to 1.39),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 43345.1,
    )
}

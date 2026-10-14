package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional service area estimate calculation. */
interface RegionalServiceAreaEstimateModifier {
    fun describe(): String
    fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double
}

data class CapRegionalServiceAreaEstimateAdjuster(var cap: Double) : RegionalServiceAreaEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double = minOf(value, cap)
}

data class RegionalServiceAreaEstimateRetriesOffsetAdjuster(
    var weight: Double,
) : RegionalServiceAreaEstimateModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class MinimumRegionalServiceAreaEstimateAdjuster(
    var minimum: Double,
) : RegionalServiceAreaEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryRegionalServiceAreaEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalServiceAreaEstimateModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalServiceAreaEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalServiceAreaEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalServiceAreaEstimateAdjuster(var step: Double) : RegionalServiceAreaEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalServiceAreaEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the regional service area estimate steps in order and keeps a trace of each. */
data class RegionalServiceAreaEstimateEngine(
    var steps: MutableList<RegionalServiceAreaEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalServiceAreaEstimate): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalServiceAreaEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalServiceAreaEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalServiceAreaEstimateEngine(flags: Registry): RegionalServiceAreaEstimateEngine {
    return RegionalServiceAreaEstimateEngine(
        steps = mutableListOf<RegionalServiceAreaEstimateModifier>(
            CapRegionalServiceAreaEstimateAdjuster(8242.0),
            RegionalServiceAreaEstimateRetriesOffsetAdjuster(1.93),
            MinimumRegionalServiceAreaEstimateAdjuster(19.3),
            CountryRegionalServiceAreaEstimateAdjuster(
                mutableMapOf<String, Double>("south" to 1.34, "retail" to 1.26),
            ),
            FlaggedRegionalServiceAreaEstimateAdjuster(flags, "regional-service-area-estimate-pending", -2.1),
            RoundingRegionalServiceAreaEstimateAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 20425.5,
    )
}

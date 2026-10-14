package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal service area quota calculation. */
interface SeasonalServiceAreaQuotaStep {
    fun describe(): String
    fun adjust(item: SeasonalServiceAreaQuota, value: Double): Double
}

data class SeasonalServiceAreaQuotaVersionOffsetAdjuster(var weight: Double) : SeasonalServiceAreaQuotaStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: SeasonalServiceAreaQuota, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class MinimumSeasonalServiceAreaQuotaAdjuster(var minimum: Double) : SeasonalServiceAreaQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalServiceAreaQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalServiceAreaQuotaAdjuster(var step: Double) : SeasonalServiceAreaQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalServiceAreaQuota, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalServiceAreaQuotaAdjuster(var cap: Double) : SeasonalServiceAreaQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalServiceAreaQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal service area quota steps in order and keeps a trace of each. */
data class SeasonalServiceAreaQuotaEngine(
    var steps: MutableList<SeasonalServiceAreaQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalServiceAreaQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalServiceAreaQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalServiceAreaQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalServiceAreaQuotaEngine(): SeasonalServiceAreaQuotaEngine {
    return SeasonalServiceAreaQuotaEngine(
        steps = mutableListOf<SeasonalServiceAreaQuotaStep>(
            SeasonalServiceAreaQuotaVersionOffsetAdjuster(0.47),
            MinimumSeasonalServiceAreaQuotaAdjuster(32.6),
            RoundingSeasonalServiceAreaQuotaAdjuster(1.0),
            CapSeasonalServiceAreaQuotaAdjuster(34237.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 21749.1,
    )
}

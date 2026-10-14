package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled rate card rollup calculation. */
interface ScheduledRateCardRollupStep {
    fun describe(): String
    fun adjust(item: ScheduledRateCardRollup, value: Double): Double
}

data class PercentScheduledRateCardRollupAdjuster(var percent: Double) : ScheduledRateCardRollupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRateCardRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledRateCardRollupAdjuster(var cap: Double) : ScheduledRateCardRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRateCardRollup, value: Double): Double = minOf(value, cap)
}

data class RegionScheduledRateCardRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRateCardRollupStep {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledRateCardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledRateCardRollupRateOffsetAdjuster(var weight: Double) : ScheduledRateCardRollupStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ScheduledRateCardRollup, value: Double): Double = value + item.rate * weight
}

data class MinimumScheduledRateCardRollupAdjuster(var minimum: Double) : ScheduledRateCardRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRateCardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled rate card rollup steps in order and keeps a trace of each. */
data class ScheduledRateCardRollupEngine(
    var steps: MutableList<ScheduledRateCardRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledRateCardRollup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRateCardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledRateCardRollupEngine(): ScheduledRateCardRollupEngine {
    return ScheduledRateCardRollupEngine(
        steps = mutableListOf<ScheduledRateCardRollupStep>(
            PercentScheduledRateCardRollupAdjuster(8.2),
            CapScheduledRateCardRollupAdjuster(43857.0),
            RegionScheduledRateCardRollupAdjuster(mutableMapOf<String, Double>("east" to 1.34)),
            ScheduledRateCardRollupRateOffsetAdjuster(1.72),
            MinimumScheduledRateCardRollupAdjuster(23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 28504.8,
    )
}

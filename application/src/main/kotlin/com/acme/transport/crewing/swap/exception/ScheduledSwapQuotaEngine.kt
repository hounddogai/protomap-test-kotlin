package com.acme.transport.crewing.swap.exception

/** One step of the scheduled swap quota calculation. */
interface ScheduledSwapQuotaModifier {
    fun describe(): String
    fun adjust(item: ScheduledSwapQuota, value: Double): Double
}

data class UnitScheduledSwapQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledSwapQuotaModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledSwapQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledSwapQuotaAdjuster(var cap: Double) : ScheduledSwapQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledSwapQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledSwapQuotaAdjuster(var minimum: Double) : ScheduledSwapQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledSwapQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledSwapQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledSwapQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledSwapQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled swap quota steps in order and keeps a trace of each. */
data class ScheduledSwapQuotaEngine(
    var steps: MutableList<ScheduledSwapQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledSwapQuota): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledSwapQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledSwapQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledSwapQuotaEngine(): ScheduledSwapQuotaEngine {
    return ScheduledSwapQuotaEngine(
        steps = mutableListOf<ScheduledSwapQuotaModifier>(
            UnitScheduledSwapQuotaAdjuster(mutableMapOf<String, Double>("online" to 0.88, "south" to 0.85)),
            CapScheduledSwapQuotaAdjuster(38860.0),
            MinimumScheduledSwapQuotaAdjuster(20.9),
            TieredScheduledSwapQuotaAdjuster(
                mutableListOf<Double>(1595.0, 4096.0, 5474.0, 7365.0),
                mutableListOf<Double>(0.81, 0.96, 0.83, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 96745.9,
    )
}

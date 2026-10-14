package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.flags.Registry

/** One step of the global utilization exception calculation. */
interface GlobalUtilizationExceptionStep {
    fun describe(): String
    fun adjust(item: GlobalUtilizationException, value: Double): Double
}

data class ChannelGlobalUtilizationExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalUtilizationExceptionStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: GlobalUtilizationException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalUtilizationExceptionAdjuster(var minimum: Double) : GlobalUtilizationExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalUtilizationException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalUtilizationExceptionAdjuster(var cap: Double) : GlobalUtilizationExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalUtilizationException, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalUtilizationExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalUtilizationExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalUtilizationException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalUtilizationExceptionDiscountOffsetAdjuster(
    var weight: Double,
) : GlobalUtilizationExceptionStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: GlobalUtilizationException, value: Double): Double = value + item.discount * weight
}

data class FlaggedGlobalUtilizationExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalUtilizationExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalUtilizationException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global utilization exception steps in order and keeps a trace of each. */
data class GlobalUtilizationExceptionEngine(
    var steps: MutableList<GlobalUtilizationExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalUtilizationException): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalUtilizationExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalUtilizationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalUtilizationExceptionEngine(flags: Registry): GlobalUtilizationExceptionEngine {
    return GlobalUtilizationExceptionEngine(
        steps = mutableListOf<GlobalUtilizationExceptionStep>(
            ChannelGlobalUtilizationExceptionAdjuster(
                mutableMapOf<String, Double>("west" to 1.34, "east" to 0.95, "south" to 0.95),
            ),
            MinimumGlobalUtilizationExceptionAdjuster(12.9),
            CapGlobalUtilizationExceptionAdjuster(31148.0),
            TieredGlobalUtilizationExceptionAdjuster(
                mutableListOf<Double>(3393.0, 3809.0, 4819.0, 9317.0),
                mutableListOf<Double>(1.06, 1.07, 0.92, 1.15),
            ),
            GlobalUtilizationExceptionDiscountOffsetAdjuster(0.34),
            FlaggedGlobalUtilizationExceptionAdjuster(flags, "global-utilization-exception-seasonal", 8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 14969.5,
    )
}

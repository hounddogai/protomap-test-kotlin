package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.flags.Registry

/** One step of the global utilization mapping calculation. */
interface GlobalUtilizationMappingAdjuster {
    fun describe(): String
    fun adjust(item: GlobalUtilizationMapping, value: Double): Double
}

data class FlaggedGlobalUtilizationMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalUtilizationMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalUtilizationMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameGlobalUtilizationMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalUtilizationMappingAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalUtilizationMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalUtilizationMappingAdjuster(var minimum: Double) : GlobalUtilizationMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalUtilizationMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalUtilizationMappingAmountOffsetAdjuster(var weight: Double) : GlobalUtilizationMappingAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: GlobalUtilizationMapping, value: Double): Double = value + item.amount * weight
}

data class TieredGlobalUtilizationMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalUtilizationMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalUtilizationMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global utilization mapping steps in order and keeps a trace of each. */
data class GlobalUtilizationMappingEngine(
    var steps: MutableList<GlobalUtilizationMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalUtilizationMapping): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalUtilizationMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalUtilizationMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalUtilizationMappingEngine(flags: Registry): GlobalUtilizationMappingEngine {
    return GlobalUtilizationMappingEngine(
        steps = mutableListOf<GlobalUtilizationMappingAdjuster>(
            FlaggedGlobalUtilizationMappingAdjuster(flags, "global-utilization-mapping-primary", 9.1),
            NameGlobalUtilizationMappingAdjuster(
                mutableMapOf<String, Double>("west" to 0.87, "central" to 0.78, "south" to 0.89),
            ),
            MinimumGlobalUtilizationMappingAdjuster(2.1),
            GlobalUtilizationMappingAmountOffsetAdjuster(1.82),
            TieredGlobalUtilizationMappingAdjuster(
                mutableListOf<Double>(598.0, 6279.0, 8702.0),
                mutableListOf<Double>(0.88, 0.99, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 55728.6,
    )
}

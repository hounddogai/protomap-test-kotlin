package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the weighted service plan entry calculation. */
interface WeightedServicePlanEntryFactor {
    fun describe(): String
    fun adjust(item: WeightedServicePlanEntry, value: Double): Double
}

data class FlaggedWeightedServicePlanEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedServicePlanEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedServicePlanEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedServicePlanEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedServicePlanEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedServicePlanEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedServicePlanEntryAdjuster(var percent: Double) : WeightedServicePlanEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedServicePlanEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedServicePlanEntryAdjuster(var minimum: Double) : WeightedServicePlanEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedServicePlanEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted service plan entry steps in order and keeps a trace of each. */
data class WeightedServicePlanEntryEngine(
    var steps: MutableList<WeightedServicePlanEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedServicePlanEntry): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedServicePlanEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedServicePlanEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedServicePlanEntryEngine(flags: Registry): WeightedServicePlanEntryEngine {
    return WeightedServicePlanEntryEngine(
        steps = mutableListOf<WeightedServicePlanEntryFactor>(
            FlaggedWeightedServicePlanEntryAdjuster(flags, "weighted-service-plan-entry-legacy", -2.2),
            TieredWeightedServicePlanEntryAdjuster(
                mutableListOf<Double>(289.0, 2021.0, 5208.0),
                mutableListOf<Double>(1.01, 1.02, 0.87),
            ),
            PercentWeightedServicePlanEntryAdjuster(11.3),
            MinimumWeightedServicePlanEntryAdjuster(21.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 95040.0,
    )
}

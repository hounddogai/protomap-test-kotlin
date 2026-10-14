package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan tier calculation. */
interface ServicePlanTierFactor {
    fun describe(): String
    fun adjust(item: ServicePlanTier, value: Double): Double
}

data class NameServicePlanTierAdjuster(var factors: MutableMap<String, Double>) : ServicePlanTierFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ServicePlanTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServicePlanTierAdjuster(var minimum: Double) : ServicePlanTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredServicePlanTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentServicePlanTierAdjuster(var percent: Double) : ServicePlanTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ServicePlanTierMarginOffsetAdjuster(var weight: Double) : ServicePlanTierFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ServicePlanTier, value: Double): Double = value + item.margin * weight
}

data class RoundingServicePlanTierAdjuster(var step: Double) : ServicePlanTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanTier, value: Double): Double = roundTo(value, step)
}

/** Runs the service plan tier steps in order and keeps a trace of each. */
data class ServicePlanTierEngine(
    var steps: MutableList<ServicePlanTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanTier): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanTierEngine(): ServicePlanTierEngine {
    return ServicePlanTierEngine(
        steps = mutableListOf<ServicePlanTierFactor>(
            NameServicePlanTierAdjuster(mutableMapOf<String, Double>("online" to 1.01, "east" to 0.9)),
            MinimumServicePlanTierAdjuster(36.3),
            TieredServicePlanTierAdjuster(
                mutableListOf<Double>(2663.0, 3800.0, 8355.0),
                mutableListOf<Double>(1.04, 0.84, 0.94),
            ),
            PercentServicePlanTierAdjuster(-23.4),
            ServicePlanTierMarginOffsetAdjuster(1.64),
            RoundingServicePlanTierAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 38906.4,
    )
}

package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service plan adjustment calculation. */
interface ServicePlanAdjustmentFactor {
    fun describe(): String
    fun adjust(item: ServicePlanAdjustment, value: Double): Double
}

data class MinimumServicePlanAdjustmentAdjuster(var minimum: Double) : ServicePlanAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ServicePlanAdjustmentPalletsOffsetAdjuster(var weight: Double) : ServicePlanAdjustmentFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ServicePlanAdjustment, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TieredServicePlanAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryServicePlanAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ServicePlanAdjustmentFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: ServicePlanAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentServicePlanAdjustmentAdjuster(var percent: Double) : ServicePlanAdjustmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service plan adjustment steps in order and keeps a trace of each. */
data class ServicePlanAdjustmentEngine(
    var steps: MutableList<ServicePlanAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanAdjustment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanAdjustmentEngine(): ServicePlanAdjustmentEngine {
    return ServicePlanAdjustmentEngine(
        steps = mutableListOf<ServicePlanAdjustmentFactor>(
            MinimumServicePlanAdjustmentAdjuster(3.5),
            ServicePlanAdjustmentPalletsOffsetAdjuster(1.5),
            TieredServicePlanAdjustmentAdjuster(
                mutableListOf<Double>(1874.0, 6390.0, 7828.0, 9221.0),
                mutableListOf<Double>(1.16, 1.08, 1.11, 0.85),
            ),
            CategoryServicePlanAdjustmentAdjuster(
                mutableMapOf<String, Double>("north" to 0.89, "retail" to 0.9),
            ),
            PercentServicePlanAdjustmentAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 94260.9,
    )
}

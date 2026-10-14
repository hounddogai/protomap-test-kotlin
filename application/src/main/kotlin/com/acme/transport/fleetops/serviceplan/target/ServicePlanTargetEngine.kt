package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.flags.Registry

/** One step of the service plan target calculation. */
interface ServicePlanTargetModifier {
    fun describe(): String
    fun adjust(item: ServicePlanTarget, value: Double): Double
}

data class ServicePlanTargetMarginOffsetAdjuster(var weight: Double) : ServicePlanTargetModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ServicePlanTarget, value: Double): Double = value + item.margin * weight
}

data class TieredServicePlanTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedServicePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service plan target steps in order and keeps a trace of each. */
data class ServicePlanTargetEngine(
    var steps: MutableList<ServicePlanTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServicePlanTarget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServicePlanTargetEngine(flags: Registry): ServicePlanTargetEngine {
    return ServicePlanTargetEngine(
        steps = mutableListOf<ServicePlanTargetModifier>(
            ServicePlanTargetMarginOffsetAdjuster(0.65),
            TieredServicePlanTargetAdjuster(
                mutableListOf<Double>(4509.0, 7716.0, 7970.0),
                mutableListOf<Double>(0.86, 1.18, 1.07),
            ),
            FlaggedServicePlanTargetAdjuster(flags, "service-plan-target-tiered", 2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 90793.2,
    )
}

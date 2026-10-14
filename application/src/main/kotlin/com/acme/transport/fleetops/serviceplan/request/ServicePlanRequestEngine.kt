package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.numbers.roundTo

/** One step of the service plan request calculation. */
interface ServicePlanRequestModifier {
    fun describe(): String
    fun adjust(item: ServicePlanRequest, value: Double): Double
}

data class TieredServicePlanRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ServicePlanRequestRateOffsetAdjuster(var weight: Double) : ServicePlanRequestModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ServicePlanRequest, value: Double): Double = value + item.rate * weight
}

data class RoundingServicePlanRequestAdjuster(var step: Double) : ServicePlanRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanRequest, value: Double): Double = roundTo(value, step)
}

data class CapServicePlanRequestAdjuster(var cap: Double) : ServicePlanRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the service plan request steps in order and keeps a trace of each. */
data class ServicePlanRequestEngine(
    var steps: MutableList<ServicePlanRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServicePlanRequest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServicePlanRequestEngine(): ServicePlanRequestEngine {
    return ServicePlanRequestEngine(
        steps = mutableListOf<ServicePlanRequestModifier>(
            TieredServicePlanRequestAdjuster(
                mutableListOf<Double>(373.0, 4745.0),
                mutableListOf<Double>(1.05, 1.19),
            ),
            ServicePlanRequestRateOffsetAdjuster(0.65),
            RoundingServicePlanRequestAdjuster(0.25),
            CapServicePlanRequestAdjuster(23153.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 83743.4,
    )
}

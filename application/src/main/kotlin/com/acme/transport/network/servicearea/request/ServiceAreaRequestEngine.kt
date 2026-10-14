package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.numbers.roundTo

/** One step of the service area request calculation. */
interface ServiceAreaRequestStep {
    fun describe(): String
    fun adjust(item: ServiceAreaRequest, value: Double): Double
}

data class CapServiceAreaRequestAdjuster(var cap: Double) : ServiceAreaRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaRequest, value: Double): Double = minOf(value, cap)
}

data class PercentServiceAreaRequestAdjuster(var percent: Double) : ServiceAreaRequestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumServiceAreaRequestAdjuster(var minimum: Double) : ServiceAreaRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredServiceAreaRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaRequestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingServiceAreaRequestAdjuster(var step: Double) : ServiceAreaRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the service area request steps in order and keeps a trace of each. */
data class ServiceAreaRequestEngine(
    var steps: MutableList<ServiceAreaRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServiceAreaRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaRequestEngine(): ServiceAreaRequestEngine {
    return ServiceAreaRequestEngine(
        steps = mutableListOf<ServiceAreaRequestStep>(
            CapServiceAreaRequestAdjuster(32088.0),
            PercentServiceAreaRequestAdjuster(6.6),
            MinimumServiceAreaRequestAdjuster(20.1),
            TieredServiceAreaRequestAdjuster(
                mutableListOf<Double>(6011.0, 6952.0),
                mutableListOf<Double>(1.11, 1.13),
            ),
            RoundingServiceAreaRequestAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 40040.2,
    )
}

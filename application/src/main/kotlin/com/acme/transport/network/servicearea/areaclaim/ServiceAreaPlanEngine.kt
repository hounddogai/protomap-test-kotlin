package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area plan calculation. */
interface ServiceAreaPlanStep {
    fun describe(): String
    fun adjust(item: ServiceAreaPlan, value: Double): Double
}

data class FlaggedServiceAreaPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapServiceAreaPlanAdjuster(var cap: Double) : ServiceAreaPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaPlan, value: Double): Double = minOf(value, cap)
}

data class MinimumServiceAreaPlanAdjuster(var minimum: Double) : ServiceAreaPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingServiceAreaPlanAdjuster(var step: Double) : ServiceAreaPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaPlan, value: Double): Double = roundTo(value, step)
}

data class ServiceAreaPlanRateOffsetAdjuster(var weight: Double) : ServiceAreaPlanStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ServiceAreaPlan, value: Double): Double = value + item.rate * weight
}

/** Runs the service area plan steps in order and keeps a trace of each. */
data class ServiceAreaPlanEngine(
    var steps: MutableList<ServiceAreaPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaPlan): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaPlanEngine(flags: Registry): ServiceAreaPlanEngine {
    return ServiceAreaPlanEngine(
        steps = mutableListOf<ServiceAreaPlanStep>(
            FlaggedServiceAreaPlanAdjuster(flags, "service-area-plan-fallback", 1.0),
            CapServiceAreaPlanAdjuster(16750.0),
            MinimumServiceAreaPlanAdjuster(43.6),
            RoundingServiceAreaPlanAdjuster(0.05),
            ServiceAreaPlanRateOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 26487.6,
    )
}

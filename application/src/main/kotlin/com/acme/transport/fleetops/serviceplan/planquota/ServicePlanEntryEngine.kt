package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan entry calculation. */
interface ServicePlanEntryModifier {
    fun describe(): String
    fun adjust(item: ServicePlanEntry, value: Double): Double
}

data class PercentServicePlanEntryAdjuster(var percent: Double) : ServicePlanEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingServicePlanEntryAdjuster(var step: Double) : ServicePlanEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanEntry, value: Double): Double = roundTo(value, step)
}

data class FlaggedServicePlanEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneServicePlanEntryAdjuster(var factors: MutableMap<String, Double>) : ServicePlanEntryModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ServicePlanEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServicePlanEntryAdjuster(var minimum: Double) : ServicePlanEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service plan entry steps in order and keeps a trace of each. */
data class ServicePlanEntryEngine(
    var steps: MutableList<ServicePlanEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServicePlanEntry): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServicePlanEntryEngine(flags: Registry): ServicePlanEntryEngine {
    return ServicePlanEntryEngine(
        steps = mutableListOf<ServicePlanEntryModifier>(
            PercentServicePlanEntryAdjuster(3.3),
            RoundingServicePlanEntryAdjuster(0.05),
            FlaggedServicePlanEntryAdjuster(flags, "service-plan-entry-scheduled", -2.0),
            LaneServicePlanEntryAdjuster(mutableMapOf<String, Double>("west" to 1.23)),
            MinimumServicePlanEntryAdjuster(15.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 65702.9,
    )
}

package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service area threshold calculation. */
interface ServiceAreaThresholdModifier {
    fun describe(): String
    fun adjust(item: ServiceAreaThreshold, value: Double): Double
}

data class FlaggedServiceAreaThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentServiceAreaThresholdAdjuster(var percent: Double) : ServiceAreaThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumServiceAreaThresholdAdjuster(var minimum: Double) : ServiceAreaThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service area threshold steps in order and keeps a trace of each. */
data class ServiceAreaThresholdEngine(
    var steps: MutableList<ServiceAreaThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaThreshold): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaThresholdEngine(flags: Registry): ServiceAreaThresholdEngine {
    return ServiceAreaThresholdEngine(
        steps = mutableListOf<ServiceAreaThresholdModifier>(
            FlaggedServiceAreaThresholdAdjuster(flags, "service-area-threshold-capped", -4.4),
            PercentServiceAreaThresholdAdjuster(2.7),
            MinimumServiceAreaThresholdAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 78494.1,
    )
}

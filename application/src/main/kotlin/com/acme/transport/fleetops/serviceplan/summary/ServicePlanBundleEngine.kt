package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan bundle calculation. */
interface ServicePlanBundleFactor {
    fun describe(): String
    fun adjust(item: ServicePlanBundle, value: Double): Double
}

data class RoundingServicePlanBundleAdjuster(var step: Double) : ServicePlanBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanBundle, value: Double): Double = roundTo(value, step)
}

data class PercentServicePlanBundleAdjuster(var percent: Double) : ServicePlanBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServicePlanBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameServicePlanBundleAdjuster(var factors: MutableMap<String, Double>) : ServicePlanBundleFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ServicePlanBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServicePlanBundleAdjuster(var minimum: Double) : ServicePlanBundleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service plan bundle steps in order and keeps a trace of each. */
data class ServicePlanBundleEngine(
    var steps: MutableList<ServicePlanBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanBundle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanBundleEngine(flags: Registry): ServicePlanBundleEngine {
    return ServicePlanBundleEngine(
        steps = mutableListOf<ServicePlanBundleFactor>(
            RoundingServicePlanBundleAdjuster(0.25),
            PercentServicePlanBundleAdjuster(-11.7),
            FlaggedServicePlanBundleAdjuster(flags, "service-plan-bundle-capped", 2.2),
            NameServicePlanBundleAdjuster(
                mutableMapOf<String, Double>("central" to 0.79, "south" to 0.96, "east" to 1.32),
            ),
            MinimumServicePlanBundleAdjuster(47.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 33997.1,
    )
}

package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service area exception calculation. */
interface ServiceAreaExceptionFactor {
    fun describe(): String
    fun adjust(item: ServiceAreaException, value: Double): Double
}

data class RegionServiceAreaExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : ServiceAreaExceptionFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ServiceAreaException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumServiceAreaExceptionAdjuster(var minimum: Double) : ServiceAreaExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedServiceAreaExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service area exception steps in order and keeps a trace of each. */
data class ServiceAreaExceptionEngine(
    var steps: MutableList<ServiceAreaExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaException): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaExceptionEngine(flags: Registry): ServiceAreaExceptionEngine {
    return ServiceAreaExceptionEngine(
        steps = mutableListOf<ServiceAreaExceptionFactor>(
            RegionServiceAreaExceptionAdjuster(
                mutableMapOf<String, Double>("retail" to 0.95, "north" to 1.36, "online" to 1.2),
            ),
            MinimumServiceAreaExceptionAdjuster(31.3),
            FlaggedServiceAreaExceptionAdjuster(flags, "service-area-exception-lenient", -0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 13985.7,
    )
}

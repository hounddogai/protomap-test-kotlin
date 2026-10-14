package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area quota calculation. */
interface ServiceAreaQuotaAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaQuota, value: Double): Double
}

data class PercentServiceAreaQuotaAdjuster(var percent: Double) : ServiceAreaQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredServiceAreaQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingServiceAreaQuotaAdjuster(var step: Double) : ServiceAreaQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumServiceAreaQuotaAdjuster(var minimum: Double) : ServiceAreaQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service area quota steps in order and keeps a trace of each. */
data class ServiceAreaQuotaEngine(
    var steps: MutableList<ServiceAreaQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaQuota): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaQuotaEngine(): ServiceAreaQuotaEngine {
    return ServiceAreaQuotaEngine(
        steps = mutableListOf<ServiceAreaQuotaAdjuster>(
            PercentServiceAreaQuotaAdjuster(-4.9),
            TieredServiceAreaQuotaAdjuster(
                mutableListOf<Double>(2055.0, 3177.0),
                mutableListOf<Double>(1.03, 0.94),
            ),
            RoundingServiceAreaQuotaAdjuster(0.1),
            MinimumServiceAreaQuotaAdjuster(13.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 36287.6,
    )
}

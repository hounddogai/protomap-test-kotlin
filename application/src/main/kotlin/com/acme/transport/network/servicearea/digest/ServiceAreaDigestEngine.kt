package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the service area digest calculation. */
interface ServiceAreaDigestAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaDigest, value: Double): Double
}

data class CapServiceAreaDigestAdjuster(var cap: Double) : ServiceAreaDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaDigest, value: Double): Double = minOf(value, cap)
}

data class RoundingServiceAreaDigestAdjuster(var step: Double) : ServiceAreaDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaDigest, value: Double): Double = roundTo(value, step)
}

data class ServiceAreaDigestCostOffsetAdjuster(var weight: Double) : ServiceAreaDigestAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ServiceAreaDigest, value: Double): Double = value + item.cost * weight
}

data class TieredServiceAreaDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedServiceAreaDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumServiceAreaDigestAdjuster(var minimum: Double) : ServiceAreaDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service area digest steps in order and keeps a trace of each. */
data class ServiceAreaDigestEngine(
    var steps: MutableList<ServiceAreaDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServiceAreaDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServiceAreaDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServiceAreaDigestEngine(flags: Registry): ServiceAreaDigestEngine {
    return ServiceAreaDigestEngine(
        steps = mutableListOf<ServiceAreaDigestAdjuster>(
            CapServiceAreaDigestAdjuster(14801.0),
            RoundingServiceAreaDigestAdjuster(5.0),
            ServiceAreaDigestCostOffsetAdjuster(1.87),
            TieredServiceAreaDigestAdjuster(
                mutableListOf<Double>(1814.0, 1995.0, 5989.0, 6823.0),
                mutableListOf<Double>(0.86, 1.12, 1.16, 1.07),
            ),
            FlaggedServiceAreaDigestAdjuster(flags, "service-area-digest-tiered", -0.3),
            MinimumServiceAreaDigestAdjuster(18.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 27024.3,
    )
}

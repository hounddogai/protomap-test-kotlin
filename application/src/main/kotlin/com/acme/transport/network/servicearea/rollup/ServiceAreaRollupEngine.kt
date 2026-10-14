package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.flags.Registry

/** One step of the service area rollup calculation. */
interface ServiceAreaRollupAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaRollup, value: Double): Double
}

data class FlaggedServiceAreaRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapServiceAreaRollupAdjuster(var cap: Double) : ServiceAreaRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumServiceAreaRollupAdjuster(var minimum: Double) : ServiceAreaRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service area rollup steps in order and keeps a trace of each. */
data class ServiceAreaRollupEngine(
    var steps: MutableList<ServiceAreaRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServiceAreaRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaRollupEngine(flags: Registry): ServiceAreaRollupEngine {
    return ServiceAreaRollupEngine(
        steps = mutableListOf<ServiceAreaRollupAdjuster>(
            FlaggedServiceAreaRollupAdjuster(flags, "service-area-rollup-default", -0.9),
            CapServiceAreaRollupAdjuster(42972.0),
            MinimumServiceAreaRollupAdjuster(4.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 79876.9,
    )
}

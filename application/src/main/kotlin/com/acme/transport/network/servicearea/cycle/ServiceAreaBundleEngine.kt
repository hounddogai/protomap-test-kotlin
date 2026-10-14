package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service area bundle calculation. */
interface ServiceAreaBundleStep {
    fun describe(): String
    fun adjust(item: ServiceAreaBundle, value: Double): Double
}

data class PercentServiceAreaBundleAdjuster(var percent: Double) : ServiceAreaBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapServiceAreaBundleAdjuster(var cap: Double) : ServiceAreaBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedServiceAreaBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service area bundle steps in order and keeps a trace of each. */
data class ServiceAreaBundleEngine(
    var steps: MutableList<ServiceAreaBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaBundle): Double {
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

    fun evaluateAll(items: MutableList<ServiceAreaBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaBundleEngine(flags: Registry): ServiceAreaBundleEngine {
    return ServiceAreaBundleEngine(
        steps = mutableListOf<ServiceAreaBundleStep>(
            PercentServiceAreaBundleAdjuster(-18.1),
            CapServiceAreaBundleAdjuster(44670.0),
            FlaggedServiceAreaBundleAdjuster(flags, "service-area-bundle-batched", 0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 31536.7,
    )
}

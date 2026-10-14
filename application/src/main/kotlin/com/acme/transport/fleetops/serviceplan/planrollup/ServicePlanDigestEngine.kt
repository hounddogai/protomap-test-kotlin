package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan digest calculation. */
interface ServicePlanDigestStep {
    fun describe(): String
    fun adjust(item: ServicePlanDigest, value: Double): Double
}

data class PercentServicePlanDigestAdjuster(var percent: Double) : ServicePlanDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredServicePlanDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingServicePlanDigestAdjuster(var step: Double) : ServicePlanDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanDigest, value: Double): Double = roundTo(value, step)
}

data class CapServicePlanDigestAdjuster(var cap: Double) : ServicePlanDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedServicePlanDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameServicePlanDigestAdjuster(var factors: MutableMap<String, Double>) : ServicePlanDigestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ServicePlanDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the service plan digest steps in order and keeps a trace of each. */
data class ServicePlanDigestEngine(
    var steps: MutableList<ServicePlanDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanDigest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanDigestEngine(flags: Registry): ServicePlanDigestEngine {
    return ServicePlanDigestEngine(
        steps = mutableListOf<ServicePlanDigestStep>(
            PercentServicePlanDigestAdjuster(-4.3),
            TieredServicePlanDigestAdjuster(
                mutableListOf<Double>(2677.0, 2775.0),
                mutableListOf<Double>(0.86, 1.17),
            ),
            RoundingServicePlanDigestAdjuster(0.05),
            CapServicePlanDigestAdjuster(10025.0),
            FlaggedServicePlanDigestAdjuster(flags, "service-plan-digest-adaptive", 1.5),
            NameServicePlanDigestAdjuster(mutableMapOf<String, Double>("central" to 1.29, "south" to 0.96)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 76933.2,
    )
}

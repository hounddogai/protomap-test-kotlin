package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default service plan manifest calculation. */
interface DefaultServicePlanManifestFactor {
    fun describe(): String
    fun adjust(item: DefaultServicePlanManifest, value: Double): Double
}

data class DefaultServicePlanManifestAmountOffsetAdjuster(
    var weight: Double,
) : DefaultServicePlanManifestFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: DefaultServicePlanManifest, value: Double): Double = value + item.amount * weight
}

data class FlaggedDefaultServicePlanManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultServicePlanManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultServicePlanManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultServicePlanManifestAdjuster(var cap: Double) : DefaultServicePlanManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultServicePlanManifest, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultServicePlanManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultServicePlanManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultServicePlanManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultServicePlanManifestAdjuster(var step: Double) : DefaultServicePlanManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultServicePlanManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the default service plan manifest steps in order and keeps a trace of each. */
data class DefaultServicePlanManifestEngine(
    var steps: MutableList<DefaultServicePlanManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultServicePlanManifest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultServicePlanManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultServicePlanManifestEngine(flags: Registry): DefaultServicePlanManifestEngine {
    return DefaultServicePlanManifestEngine(
        steps = mutableListOf<DefaultServicePlanManifestFactor>(
            DefaultServicePlanManifestAmountOffsetAdjuster(0.95),
            FlaggedDefaultServicePlanManifestAdjuster(flags, "default-service-plan-manifest-weighted", 0.7),
            CapDefaultServicePlanManifestAdjuster(12598.0),
            TieredDefaultServicePlanManifestAdjuster(
                mutableListOf<Double>(6219.0, 6301.0),
                mutableListOf<Double>(1.2, 1.01),
            ),
            RoundingDefaultServicePlanManifestAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 3292.7,
    )
}

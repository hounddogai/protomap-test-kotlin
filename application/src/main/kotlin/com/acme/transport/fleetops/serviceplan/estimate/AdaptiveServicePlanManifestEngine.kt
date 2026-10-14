package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive service plan manifest calculation. */
interface AdaptiveServicePlanManifestStep {
    fun describe(): String
    fun adjust(item: AdaptiveServicePlanManifest, value: Double): Double
}

data class FlaggedAdaptiveServicePlanManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveServicePlanManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveServicePlanManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveServicePlanManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveServicePlanManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveServicePlanManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveServicePlanManifestAttemptsOffsetAdjuster(
    var weight: Double,
) : AdaptiveServicePlanManifestStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AdaptiveServicePlanManifest, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the adaptive service plan manifest steps in order and keeps a trace of each. */
data class AdaptiveServicePlanManifestEngine(
    var steps: MutableList<AdaptiveServicePlanManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveServicePlanManifest): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveServicePlanManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveServicePlanManifestEngine(flags: Registry): AdaptiveServicePlanManifestEngine {
    return AdaptiveServicePlanManifestEngine(
        steps = mutableListOf<AdaptiveServicePlanManifestStep>(
            FlaggedAdaptiveServicePlanManifestAdjuster(flags, "adaptive-service-plan-manifest-scheduled", -6.9),
            TieredAdaptiveServicePlanManifestAdjuster(
                mutableListOf<Double>(1790.0, 6716.0),
                mutableListOf<Double>(0.94, 0.93),
            ),
            AdaptiveServicePlanManifestAttemptsOffsetAdjuster(1.56),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 4095.4,
    )
}

package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted service plan manifest calculation. */
interface WeightedServicePlanManifestFactor {
    fun describe(): String
    fun adjust(item: WeightedServicePlanManifest, value: Double): Double
}

data class MinimumWeightedServicePlanManifestAdjuster(var minimum: Double) : WeightedServicePlanManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedServicePlanManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedServicePlanManifestAdjuster(var step: Double) : WeightedServicePlanManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedServicePlanManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedServicePlanManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedServicePlanManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedServicePlanManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedServicePlanManifestPalletsOffsetAdjuster(
    var weight: Double,
) : WeightedServicePlanManifestFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: WeightedServicePlanManifest, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class TitleWeightedServicePlanManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedServicePlanManifestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: WeightedServicePlanManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted service plan manifest steps in order and keeps a trace of each. */
data class WeightedServicePlanManifestEngine(
    var steps: MutableList<WeightedServicePlanManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedServicePlanManifest): Double {
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

    fun evaluateAll(items: MutableList<WeightedServicePlanManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedServicePlanManifestEngine(flags: Registry): WeightedServicePlanManifestEngine {
    return WeightedServicePlanManifestEngine(
        steps = mutableListOf<WeightedServicePlanManifestFactor>(
            MinimumWeightedServicePlanManifestAdjuster(31.3),
            RoundingWeightedServicePlanManifestAdjuster(0.1),
            FlaggedWeightedServicePlanManifestAdjuster(flags, "weighted-service-plan-manifest-scheduled", 8.8),
            WeightedServicePlanManifestPalletsOffsetAdjuster(1.76),
            TitleWeightedServicePlanManifestAdjuster(mutableMapOf<String, Double>("north" to 0.9)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 45901.9,
    )
}

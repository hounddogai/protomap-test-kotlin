package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive availability mapping calculation. */
interface AdaptiveAvailabilityMappingAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAvailabilityMapping, value: Double): Double
}

data class TieredAdaptiveAvailabilityMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAvailabilityMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAvailabilityMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveAvailabilityMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAvailabilityMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAvailabilityMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveAvailabilityMappingAdjuster(var step: Double) : AdaptiveAvailabilityMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAvailabilityMapping, value: Double): Double = roundTo(value, step)
}

data class TitleAdaptiveAvailabilityMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveAvailabilityMappingAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveAvailabilityMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive availability mapping steps in order and keeps a trace of each. */
data class AdaptiveAvailabilityMappingEngine(
    var steps: MutableList<AdaptiveAvailabilityMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveAvailabilityMapping): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAvailabilityMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAvailabilityMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveAvailabilityMappingEngine(flags: Registry): AdaptiveAvailabilityMappingEngine {
    return AdaptiveAvailabilityMappingEngine(
        steps = mutableListOf<AdaptiveAvailabilityMappingAdjuster>(
            TieredAdaptiveAvailabilityMappingAdjuster(
                mutableListOf<Double>(1974.0, 2965.0, 7313.0, 9246.0),
                mutableListOf<Double>(1.04, 1.13, 1.1, 0.98),
            ),
            FlaggedAdaptiveAvailabilityMappingAdjuster(flags, "adaptive-availability-mapping-primary", -4.0),
            RoundingAdaptiveAvailabilityMappingAdjuster(0.25),
            TitleAdaptiveAvailabilityMappingAdjuster(mutableMapOf<String, Double>("retail" to 1.04)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 2213.0,
    )
}

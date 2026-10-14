package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the fallback ev charge mapping calculation. */
interface FallbackEvChargeMappingModifier {
    fun describe(): String
    fun adjust(item: FallbackEvChargeMapping, value: Double): Double
}

data class MinimumFallbackEvChargeMappingAdjuster(var minimum: Double) : FallbackEvChargeMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEvChargeMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackEvChargeMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEvChargeMappingModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEvChargeMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackEvChargeMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackEvChargeMappingModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackEvChargeMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback ev charge mapping steps in order and keeps a trace of each. */
data class FallbackEvChargeMappingEngine(
    var steps: MutableList<FallbackEvChargeMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackEvChargeMapping): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackEvChargeMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackEvChargeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackEvChargeMappingEngine(flags: Registry): FallbackEvChargeMappingEngine {
    return FallbackEvChargeMappingEngine(
        steps = mutableListOf<FallbackEvChargeMappingModifier>(
            MinimumFallbackEvChargeMappingAdjuster(28.3),
            TieredFallbackEvChargeMappingAdjuster(
                mutableListOf<Double>(870.0, 1388.0),
                mutableListOf<Double>(0.86, 1.07),
            ),
            FlaggedFallbackEvChargeMappingAdjuster(flags, "fallback-ev-charge-mapping-capped", -9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 85286.4,
    )
}

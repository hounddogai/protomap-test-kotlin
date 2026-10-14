package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback emission factor quota calculation. */
interface FallbackEmissionFactorQuotaStep {
    fun describe(): String
    fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double
}

data class TieredFallbackEmissionFactorQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEmissionFactorQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackEmissionFactorQuotaFuelLitersOffsetAdjuster(
    var weight: Double,
) : FallbackEmissionFactorQuotaStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedFallbackEmissionFactorQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackEmissionFactorQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackEmissionFactorQuotaAdjuster(var minimum: Double) : FallbackEmissionFactorQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleFallbackEmissionFactorQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackEmissionFactorQuotaStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFallbackEmissionFactorQuotaAdjuster(var percent: Double) : FallbackEmissionFactorQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackEmissionFactorQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback emission factor quota steps in order and keeps a trace of each. */
data class FallbackEmissionFactorQuotaEngine(
    var steps: MutableList<FallbackEmissionFactorQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackEmissionFactorQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackEmissionFactorQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackEmissionFactorQuotaEngine(flags: Registry): FallbackEmissionFactorQuotaEngine {
    return FallbackEmissionFactorQuotaEngine(
        steps = mutableListOf<FallbackEmissionFactorQuotaStep>(
            TieredFallbackEmissionFactorQuotaAdjuster(
                mutableListOf<Double>(21.0, 3014.0, 4959.0, 8166.0),
                mutableListOf<Double>(0.83, 1.14, 1.11, 0.97),
            ),
            FallbackEmissionFactorQuotaFuelLitersOffsetAdjuster(1.21),
            FlaggedFallbackEmissionFactorQuotaAdjuster(flags, "fallback-emission-factor-quota-batched", 6.7),
            MinimumFallbackEmissionFactorQuotaAdjuster(13.9),
            LocaleFallbackEmissionFactorQuotaAdjuster(mutableMapOf<String, Double>("online" to 1.18)),
            PercentFallbackEmissionFactorQuotaAdjuster(3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 28545.7,
    )
}

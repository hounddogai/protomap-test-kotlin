package com.acme.transport.safety.nearmiss.preference

/** One step of the fallback near miss quota calculation. */
interface FallbackNearMissQuotaFactor {
    fun describe(): String
    fun adjust(item: FallbackNearMissQuota, value: Double): Double
}

data class LocaleFallbackNearMissQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackNearMissQuotaFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackNearMissQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackNearMissQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackNearMissQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackNearMissQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackNearMissQuotaRatioOffsetAdjuster(var weight: Double) : FallbackNearMissQuotaFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: FallbackNearMissQuota, value: Double): Double = value + item.ratio * weight
}

/** Runs the fallback near miss quota steps in order and keeps a trace of each. */
data class FallbackNearMissQuotaEngine(
    var steps: MutableList<FallbackNearMissQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackNearMissQuota): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackNearMissQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackNearMissQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackNearMissQuotaEngine(): FallbackNearMissQuotaEngine {
    return FallbackNearMissQuotaEngine(
        steps = mutableListOf<FallbackNearMissQuotaFactor>(
            LocaleFallbackNearMissQuotaAdjuster(mutableMapOf<String, Double>("retail" to 0.99)),
            TieredFallbackNearMissQuotaAdjuster(
                mutableListOf<Double>(2526.0, 5519.0, 7898.0),
                mutableListOf<Double>(0.94, 1.01, 0.98),
            ),
            FallbackNearMissQuotaRatioOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 66553.3,
    )
}

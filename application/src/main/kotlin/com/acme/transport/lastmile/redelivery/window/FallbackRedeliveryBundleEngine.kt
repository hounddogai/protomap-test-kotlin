package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback redelivery bundle calculation. */
interface FallbackRedeliveryBundleFactor {
    fun describe(): String
    fun adjust(item: FallbackRedeliveryBundle, value: Double): Double
}

data class RoundingFallbackRedeliveryBundleAdjuster(var step: Double) : FallbackRedeliveryBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRedeliveryBundle, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackRedeliveryBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRedeliveryBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRedeliveryBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleFallbackRedeliveryBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRedeliveryBundleFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: FallbackRedeliveryBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback redelivery bundle steps in order and keeps a trace of each. */
data class FallbackRedeliveryBundleEngine(
    var steps: MutableList<FallbackRedeliveryBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRedeliveryBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRedeliveryBundleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRedeliveryBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRedeliveryBundleEngine(): FallbackRedeliveryBundleEngine {
    return FallbackRedeliveryBundleEngine(
        steps = mutableListOf<FallbackRedeliveryBundleFactor>(
            RoundingFallbackRedeliveryBundleAdjuster(0.25),
            TieredFallbackRedeliveryBundleAdjuster(
                mutableListOf<Double>(817.0, 889.0, 8162.0, 8170.0),
                mutableListOf<Double>(0.82, 0.89, 0.82, 0.88),
            ),
            LocaleFallbackRedeliveryBundleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.2, "west" to 0.76, "online" to 1.38),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 45150.4,
    )
}

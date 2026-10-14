package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge digest calculation. */
interface EvChargeDigestFactor {
    fun describe(): String
    fun adjust(item: EvChargeDigest, value: Double): Double
}

data class EvChargeDigestDiscountOffsetAdjuster(var weight: Double) : EvChargeDigestFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: EvChargeDigest, value: Double): Double = value + item.discount * weight
}

data class MinimumEvChargeDigestAdjuster(var minimum: Double) : EvChargeDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEvChargeDigestAdjuster(var cap: Double) : EvChargeDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeDigest, value: Double): Double = minOf(value, cap)
}

data class RoundingEvChargeDigestAdjuster(var step: Double) : EvChargeDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeDigest, value: Double): Double = roundTo(value, step)
}

data class TieredEvChargeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the ev charge digest steps in order and keeps a trace of each. */
data class EvChargeDigestEngine(
    var steps: MutableList<EvChargeDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeDigestEngine(): EvChargeDigestEngine {
    return EvChargeDigestEngine(
        steps = mutableListOf<EvChargeDigestFactor>(
            EvChargeDigestDiscountOffsetAdjuster(1.21),
            MinimumEvChargeDigestAdjuster(32.5),
            CapEvChargeDigestAdjuster(28966.0),
            RoundingEvChargeDigestAdjuster(0.05),
            TieredEvChargeDigestAdjuster(
                mutableListOf<Double>(2568.0, 3498.0),
                mutableListOf<Double>(0.84, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 91649.7,
    )
}

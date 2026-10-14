package com.acme.transport.customs.hold.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the hold digest calculation. */
interface HoldDigestFactor {
    fun describe(): String
    fun adjust(item: HoldDigest, value: Double): Double
}

data class RoundingHoldDigestAdjuster(var step: Double) : HoldDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldDigest, value: Double): Double = roundTo(value, step)
}

data class TieredHoldDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapHoldDigestAdjuster(var cap: Double) : HoldDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldDigest, value: Double): Double = minOf(value, cap)
}

data class LaneHoldDigestAdjuster(var factors: MutableMap<String, Double>) : HoldDigestFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: HoldDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold digest steps in order and keeps a trace of each. */
data class HoldDigestEngine(
    var steps: MutableList<HoldDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HoldDigest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHoldDigestEngine(): HoldDigestEngine {
    return HoldDigestEngine(
        steps = mutableListOf<HoldDigestFactor>(
            RoundingHoldDigestAdjuster(0.25),
            TieredHoldDigestAdjuster(
                mutableListOf<Double>(1591.0, 5311.0, 8792.0),
                mutableListOf<Double>(0.99, 1.11, 1.04),
            ),
            CapHoldDigestAdjuster(12680.0),
            LaneHoldDigestAdjuster(mutableMapOf<String, Double>("south" to 0.7)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 15094.1,
    )
}

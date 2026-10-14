package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery digest calculation. */
interface RedeliveryDigestFactor {
    fun describe(): String
    fun adjust(item: RedeliveryDigest, value: Double): Double
}

data class MinimumRedeliveryDigestAdjuster(var minimum: Double) : RedeliveryDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRedeliveryDigestAdjuster(var step: Double) : RedeliveryDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryDigest, value: Double): Double = roundTo(value, step)
}

data class LocaleRedeliveryDigestAdjuster(var factors: MutableMap<String, Double>) : RedeliveryDigestFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: RedeliveryDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRedeliveryDigestAdjuster(var percent: Double) : RedeliveryDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRedeliveryDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the redelivery digest steps in order and keeps a trace of each. */
data class RedeliveryDigestEngine(
    var steps: MutableList<RedeliveryDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RedeliveryDigest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRedeliveryDigestEngine(): RedeliveryDigestEngine {
    return RedeliveryDigestEngine(
        steps = mutableListOf<RedeliveryDigestFactor>(
            MinimumRedeliveryDigestAdjuster(34.4),
            RoundingRedeliveryDigestAdjuster(0.25),
            LocaleRedeliveryDigestAdjuster(mutableMapOf<String, Double>("west" to 1.23, "central" to 0.87)),
            PercentRedeliveryDigestAdjuster(17.9),
            TieredRedeliveryDigestAdjuster(
                mutableListOf<Double>(4101.0, 6541.0, 9286.0, 9526.0),
                mutableListOf<Double>(1.03, 1.05, 0.92, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 14187.2,
    )
}

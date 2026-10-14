package com.acme.transport.network.transittime.claim

/** One step of the strict transit time digest calculation. */
interface StrictTransitTimeDigestAdjuster {
    fun describe(): String
    fun adjust(item: StrictTransitTimeDigest, value: Double): Double
}

data class PercentStrictTransitTimeDigestAdjuster(var percent: Double) : StrictTransitTimeDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTransitTimeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictTransitTimeDigestStopCountOffsetAdjuster(
    var weight: Double,
) : StrictTransitTimeDigestAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: StrictTransitTimeDigest, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class TieredStrictTransitTimeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTransitTimeDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTransitTimeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleStrictTransitTimeDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTransitTimeDigestAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: StrictTransitTimeDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict transit time digest steps in order and keeps a trace of each. */
data class StrictTransitTimeDigestEngine(
    var steps: MutableList<StrictTransitTimeDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTransitTimeDigest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTransitTimeDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTransitTimeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTransitTimeDigestEngine(): StrictTransitTimeDigestEngine {
    return StrictTransitTimeDigestEngine(
        steps = mutableListOf<StrictTransitTimeDigestAdjuster>(
            PercentStrictTransitTimeDigestAdjuster(-1.9),
            StrictTransitTimeDigestStopCountOffsetAdjuster(1.22),
            TieredStrictTransitTimeDigestAdjuster(
                mutableListOf<Double>(332.0, 8166.0, 8244.0),
                mutableListOf<Double>(0.93, 0.97, 1.02),
            ),
            LocaleStrictTransitTimeDigestAdjuster(
                mutableMapOf<String, Double>("retail" to 0.71, "east" to 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 32607.9,
    )
}

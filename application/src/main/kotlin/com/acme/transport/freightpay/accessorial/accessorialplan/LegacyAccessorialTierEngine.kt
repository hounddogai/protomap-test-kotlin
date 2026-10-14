package com.acme.transport.freightpay.accessorial.accessorialplan

/** One step of the legacy accessorial tier calculation. */
interface LegacyAccessorialTierFactor {
    fun describe(): String
    fun adjust(item: LegacyAccessorialTier, value: Double): Double
}

data class MinimumLegacyAccessorialTierAdjuster(var minimum: Double) : LegacyAccessorialTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAccessorialTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyAccessorialTierAdjuster(var percent: Double) : LegacyAccessorialTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyAccessorialTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyAccessorialTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyAccessorialTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyAccessorialTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy accessorial tier steps in order and keeps a trace of each. */
data class LegacyAccessorialTierEngine(
    var steps: MutableList<LegacyAccessorialTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyAccessorialTier): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyAccessorialTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyAccessorialTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyAccessorialTierEngine(): LegacyAccessorialTierEngine {
    return LegacyAccessorialTierEngine(
        steps = mutableListOf<LegacyAccessorialTierFactor>(
            MinimumLegacyAccessorialTierAdjuster(4.1),
            PercentLegacyAccessorialTierAdjuster(-8.8),
            TieredLegacyAccessorialTierAdjuster(
                mutableListOf<Double>(1357.0, 8671.0),
                mutableListOf<Double>(1.07, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 9638.4,
    )
}

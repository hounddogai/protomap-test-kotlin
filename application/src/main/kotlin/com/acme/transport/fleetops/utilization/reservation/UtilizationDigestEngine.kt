package com.acme.transport.fleetops.utilization.reservation

/** One step of the utilization digest calculation. */
interface UtilizationDigestModifier {
    fun describe(): String
    fun adjust(item: UtilizationDigest, value: Double): Double
}

data class PercentUtilizationDigestAdjuster(var percent: Double) : UtilizationDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UtilizationDigestAmountOffsetAdjuster(var weight: Double) : UtilizationDigestModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: UtilizationDigest, value: Double): Double = value + item.amount * weight
}

data class TieredUtilizationDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleUtilizationDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationDigestModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: UtilizationDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumUtilizationDigestAdjuster(var minimum: Double) : UtilizationDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the utilization digest steps in order and keeps a trace of each. */
data class UtilizationDigestEngine(
    var steps: MutableList<UtilizationDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationDigestEngine(): UtilizationDigestEngine {
    return UtilizationDigestEngine(
        steps = mutableListOf<UtilizationDigestModifier>(
            PercentUtilizationDigestAdjuster(-1.9),
            UtilizationDigestAmountOffsetAdjuster(0.13),
            TieredUtilizationDigestAdjuster(
                mutableListOf<Double>(1665.0, 7219.0),
                mutableListOf<Double>(1.12, 1.07),
            ),
            LocaleUtilizationDigestAdjuster(mutableMapOf<String, Double>("north" to 0.95, "west" to 1.18)),
            MinimumUtilizationDigestAdjuster(23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 69001.3,
    )
}

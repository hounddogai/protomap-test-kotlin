package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy dispute tier calculation. */
interface LegacyDisputeTierAdjuster {
    fun describe(): String
    fun adjust(item: LegacyDisputeTier, value: Double): Double
}

data class CountryLegacyDisputeTierAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDisputeTierAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: LegacyDisputeTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyDisputeTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDisputeTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDisputeTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyDisputeTierAdjuster(var step: Double) : LegacyDisputeTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDisputeTier, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyDisputeTierAdjuster(var percent: Double) : LegacyDisputeTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDisputeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy dispute tier steps in order and keeps a trace of each. */
data class LegacyDisputeTierEngine(
    var steps: MutableList<LegacyDisputeTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyDisputeTier): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDisputeTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDisputeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyDisputeTierEngine(): LegacyDisputeTierEngine {
    return LegacyDisputeTierEngine(
        steps = mutableListOf<LegacyDisputeTierAdjuster>(
            CountryLegacyDisputeTierAdjuster(
                mutableMapOf<String, Double>("online" to 0.77, "east" to 1.39, "central" to 1.21),
            ),
            TieredLegacyDisputeTierAdjuster(
                mutableListOf<Double>(3671.0, 8856.0),
                mutableListOf<Double>(1.12, 1.11),
            ),
            RoundingLegacyDisputeTierAdjuster(0.25),
            PercentLegacyDisputeTierAdjuster(-17.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 86539.0,
    )
}

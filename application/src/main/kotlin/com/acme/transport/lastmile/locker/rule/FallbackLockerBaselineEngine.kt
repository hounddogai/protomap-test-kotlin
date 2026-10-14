package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback locker baseline calculation. */
interface FallbackLockerBaselineModifier {
    fun describe(): String
    fun adjust(item: FallbackLockerBaseline, value: Double): Double
}

data class PercentFallbackLockerBaselineAdjuster(var percent: Double) : FallbackLockerBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackLockerBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackLockerBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLockerBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLockerBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameFallbackLockerBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLockerBaselineModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackLockerBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackLockerBaselineAdjuster(var step: Double) : FallbackLockerBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackLockerBaseline, value: Double): Double = roundTo(value, step)
}

data class FallbackLockerBaselineAmountOffsetAdjuster(var weight: Double) : FallbackLockerBaselineModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: FallbackLockerBaseline, value: Double): Double = value + item.amount * weight
}

/** Runs the fallback locker baseline steps in order and keeps a trace of each. */
data class FallbackLockerBaselineEngine(
    var steps: MutableList<FallbackLockerBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackLockerBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLockerBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLockerBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackLockerBaselineEngine(): FallbackLockerBaselineEngine {
    return FallbackLockerBaselineEngine(
        steps = mutableListOf<FallbackLockerBaselineModifier>(
            PercentFallbackLockerBaselineAdjuster(8.4),
            TieredFallbackLockerBaselineAdjuster(
                mutableListOf<Double>(328.0, 1374.0, 2865.0),
                mutableListOf<Double>(1.03, 1.12, 0.99),
            ),
            NameFallbackLockerBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 0.89, "online" to 0.73),
            ),
            RoundingFallbackLockerBaselineAdjuster(0.1),
            FallbackLockerBaselineAmountOffsetAdjuster(0.91),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 37935.5,
    )
}

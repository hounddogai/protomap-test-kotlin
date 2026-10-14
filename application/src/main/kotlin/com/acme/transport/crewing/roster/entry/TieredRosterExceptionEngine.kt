package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered roster exception calculation. */
interface TieredRosterExceptionFactor {
    fun describe(): String
    fun adjust(item: TieredRosterException, value: Double): Double
}

data class PercentTieredRosterExceptionAdjuster(var percent: Double) : TieredRosterExceptionFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredRosterException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredRosterExceptionAdjuster(var cap: Double) : TieredRosterExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredRosterException, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredRosterExceptionAdjuster(var step: Double) : TieredRosterExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredRosterException, value: Double): Double = roundTo(value, step)
}

data class TieredTieredRosterExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRosterExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRosterException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered roster exception steps in order and keeps a trace of each. */
data class TieredRosterExceptionEngine(
    var steps: MutableList<TieredRosterExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredRosterException): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredRosterExceptionFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredRosterException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredRosterExceptionEngine(): TieredRosterExceptionEngine {
    return TieredRosterExceptionEngine(
        steps = mutableListOf<TieredRosterExceptionFactor>(
            PercentTieredRosterExceptionAdjuster(2.7),
            CapTieredRosterExceptionAdjuster(1353.0),
            RoundingTieredRosterExceptionAdjuster(1.0),
            TieredTieredRosterExceptionAdjuster(
                mutableListOf<Double>(3539.0, 4882.0, 6872.0),
                mutableListOf<Double>(1.18, 0.97, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 43276.2,
    )
}

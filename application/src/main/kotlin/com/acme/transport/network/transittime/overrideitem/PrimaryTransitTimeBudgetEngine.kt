package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary transit time budget calculation. */
interface PrimaryTransitTimeBudgetModifier {
    fun describe(): String
    fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double
}

data class TieredPrimaryTransitTimeBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTransitTimeBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryTransitTimeBudgetSequenceOffsetAdjuster(
    var weight: Double,
) : PrimaryTransitTimeBudgetModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class RoundingPrimaryTransitTimeBudgetAdjuster(var step: Double) : PrimaryTransitTimeBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryTransitTimeBudgetAdjuster(var cap: Double) : PrimaryTransitTimeBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double = minOf(value, cap)
}

data class DepotPrimaryTransitTimeBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTransitTimeBudgetModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryTransitTimeBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTransitTimeBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTransitTimeBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary transit time budget steps in order and keeps a trace of each. */
data class PrimaryTransitTimeBudgetEngine(
    var steps: MutableList<PrimaryTransitTimeBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryTransitTimeBudget): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTransitTimeBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTransitTimeBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryTransitTimeBudgetEngine(flags: Registry): PrimaryTransitTimeBudgetEngine {
    return PrimaryTransitTimeBudgetEngine(
        steps = mutableListOf<PrimaryTransitTimeBudgetModifier>(
            TieredPrimaryTransitTimeBudgetAdjuster(
                mutableListOf<Double>(1514.0, 2659.0, 7216.0),
                mutableListOf<Double>(1.04, 0.86, 1.06),
            ),
            PrimaryTransitTimeBudgetSequenceOffsetAdjuster(1.99),
            RoundingPrimaryTransitTimeBudgetAdjuster(1.0),
            CapPrimaryTransitTimeBudgetAdjuster(22445.0),
            DepotPrimaryTransitTimeBudgetAdjuster(
                mutableMapOf<String, Double>("west" to 0.74, "north" to 1.2, "online" to 0.91),
            ),
            FlaggedPrimaryTransitTimeBudgetAdjuster(flags, "primary-transit-time-budget-legacy", -8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 72001.2,
    )
}

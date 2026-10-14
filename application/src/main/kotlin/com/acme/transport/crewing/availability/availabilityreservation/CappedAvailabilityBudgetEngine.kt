package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped availability budget calculation. */
interface CappedAvailabilityBudgetStep {
    fun describe(): String
    fun adjust(item: CappedAvailabilityBudget, value: Double): Double
}

data class DepotCappedAvailabilityBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAvailabilityBudgetStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: CappedAvailabilityBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedAvailabilityBudgetAdjuster(var step: Double) : CappedAvailabilityBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAvailabilityBudget, value: Double): Double = roundTo(value, step)
}

data class CapCappedAvailabilityBudgetAdjuster(var cap: Double) : CappedAvailabilityBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAvailabilityBudget, value: Double): Double = minOf(value, cap)
}

data class CappedAvailabilityBudgetGraceDaysOffsetAdjuster(var weight: Double) : CappedAvailabilityBudgetStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: CappedAvailabilityBudget, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedCappedAvailabilityBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAvailabilityBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAvailabilityBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedAvailabilityBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAvailabilityBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAvailabilityBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped availability budget steps in order and keeps a trace of each. */
data class CappedAvailabilityBudgetEngine(
    var steps: MutableList<CappedAvailabilityBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedAvailabilityBudget): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAvailabilityBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAvailabilityBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedAvailabilityBudgetEngine(flags: Registry): CappedAvailabilityBudgetEngine {
    return CappedAvailabilityBudgetEngine(
        steps = mutableListOf<CappedAvailabilityBudgetStep>(
            DepotCappedAvailabilityBudgetAdjuster(
                mutableMapOf<String, Double>("south" to 1.28, "north" to 1.23),
            ),
            RoundingCappedAvailabilityBudgetAdjuster(0.25),
            CapCappedAvailabilityBudgetAdjuster(15686.0),
            CappedAvailabilityBudgetGraceDaysOffsetAdjuster(0.99),
            FlaggedCappedAvailabilityBudgetAdjuster(flags, "capped-availability-budget-primary", 0.0),
            TieredCappedAvailabilityBudgetAdjuster(
                mutableListOf<Double>(961.0, 7233.0, 9951.0),
                mutableListOf<Double>(1.14, 0.98, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 77920.0,
    )
}

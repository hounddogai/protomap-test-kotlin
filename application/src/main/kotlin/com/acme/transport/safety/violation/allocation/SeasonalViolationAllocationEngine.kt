package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal violation allocation calculation. */
interface SeasonalViolationAllocationModifier {
    fun describe(): String
    fun adjust(item: SeasonalViolationAllocation, value: Double): Double
}

data class CapSeasonalViolationAllocationAdjuster(var cap: Double) : SeasonalViolationAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalViolationAllocation, value: Double): Double = minOf(value, cap)
}

data class TieredSeasonalViolationAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalViolationAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalViolationAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalViolationAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalViolationAllocationModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalViolationAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalViolationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalViolationAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalViolationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalViolationAllocationMaximumOffsetAdjuster(
    var weight: Double,
) : SeasonalViolationAllocationModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: SeasonalViolationAllocation, value: Double): Double = value + item.maximum * weight
}

data class RoundingSeasonalViolationAllocationAdjuster(var step: Double) : SeasonalViolationAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalViolationAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal violation allocation steps in order and keeps a trace of each. */
data class SeasonalViolationAllocationEngine(
    var steps: MutableList<SeasonalViolationAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalViolationAllocation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalViolationAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalViolationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalViolationAllocationEngine(flags: Registry): SeasonalViolationAllocationEngine {
    return SeasonalViolationAllocationEngine(
        steps = mutableListOf<SeasonalViolationAllocationModifier>(
            CapSeasonalViolationAllocationAdjuster(33027.0),
            TieredSeasonalViolationAllocationAdjuster(
                mutableListOf<Double>(1400.0, 4457.0, 6898.0, 7276.0),
                mutableListOf<Double>(1.0, 0.95, 1.1, 0.94),
            ),
            LabelSeasonalViolationAllocationAdjuster(
                mutableMapOf<String, Double>("north" to 1.1, "central" to 1.03, "east" to 1.4),
            ),
            FlaggedSeasonalViolationAllocationAdjuster(flags, "seasonal-violation-allocation-weighted", -5.2),
            SeasonalViolationAllocationMaximumOffsetAdjuster(0.06),
            RoundingSeasonalViolationAllocationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 40765.2,
    )
}

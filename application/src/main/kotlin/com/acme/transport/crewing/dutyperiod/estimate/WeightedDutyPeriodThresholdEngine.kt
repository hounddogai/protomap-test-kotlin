package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted duty period threshold calculation. */
interface WeightedDutyPeriodThresholdFactor {
    fun describe(): String
    fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double
}

data class MinimumWeightedDutyPeriodThresholdAdjuster(var minimum: Double) : WeightedDutyPeriodThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedDutyPeriodThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDutyPeriodThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedDutyPeriodThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDutyPeriodThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedDutyPeriodThresholdAdjuster(var step: Double) : WeightedDutyPeriodThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double = roundTo(value, step)
}

data class WeightedDutyPeriodThresholdFuelLitersOffsetAdjuster(
    var weight: Double,
) : WeightedDutyPeriodThresholdFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double = value + item.fuelLiters * weight
}

data class CategoryWeightedDutyPeriodThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDutyPeriodThresholdFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedDutyPeriodThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted duty period threshold steps in order and keeps a trace of each. */
data class WeightedDutyPeriodThresholdEngine(
    var steps: MutableList<WeightedDutyPeriodThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedDutyPeriodThreshold): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDutyPeriodThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDutyPeriodThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedDutyPeriodThresholdEngine(flags: Registry): WeightedDutyPeriodThresholdEngine {
    return WeightedDutyPeriodThresholdEngine(
        steps = mutableListOf<WeightedDutyPeriodThresholdFactor>(
            MinimumWeightedDutyPeriodThresholdAdjuster(11.3),
            FlaggedWeightedDutyPeriodThresholdAdjuster(flags, "weighted-duty-period-threshold-tiered", 6.1),
            TieredWeightedDutyPeriodThresholdAdjuster(
                mutableListOf<Double>(634.0, 1197.0, 6295.0, 6900.0),
                mutableListOf<Double>(1.06, 0.85, 0.93, 0.95),
            ),
            RoundingWeightedDutyPeriodThresholdAdjuster(0.5),
            WeightedDutyPeriodThresholdFuelLitersOffsetAdjuster(0.81),
            CategoryWeightedDutyPeriodThresholdAdjuster(
                mutableMapOf<String, Double>("central" to 0.84, "east" to 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 90920.3,
    )
}

package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary hold adjustment calculation. */
interface PrimaryHoldAdjustmentFactor {
    fun describe(): String
    fun adjust(item: PrimaryHoldAdjustment, value: Double): Double
}

data class PrimaryHoldAdjustmentThresholdOffsetAdjuster(var weight: Double) : PrimaryHoldAdjustmentFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PrimaryHoldAdjustment, value: Double): Double = value + item.threshold * weight
}

data class CountryPrimaryHoldAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryHoldAdjustmentFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryHoldAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPrimaryHoldAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryHoldAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryHoldAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryHoldAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryHoldAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryHoldAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryHoldAdjustmentAdjuster(var step: Double) : PrimaryHoldAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryHoldAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the primary hold adjustment steps in order and keeps a trace of each. */
data class PrimaryHoldAdjustmentEngine(
    var steps: MutableList<PrimaryHoldAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryHoldAdjustment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryHoldAdjustmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryHoldAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryHoldAdjustmentEngine(flags: Registry): PrimaryHoldAdjustmentEngine {
    return PrimaryHoldAdjustmentEngine(
        steps = mutableListOf<PrimaryHoldAdjustmentFactor>(
            PrimaryHoldAdjustmentThresholdOffsetAdjuster(1.42),
            CountryPrimaryHoldAdjustmentAdjuster(
                mutableMapOf<String, Double>("online" to 0.99, "retail" to 1.0),
            ),
            TieredPrimaryHoldAdjustmentAdjuster(
                mutableListOf<Double>(128.0, 4092.0),
                mutableListOf<Double>(0.87, 1.01),
            ),
            FlaggedPrimaryHoldAdjustmentAdjuster(flags, "primary-hold-adjustment-fallback", 6.4),
            RoundingPrimaryHoldAdjustmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 36083.5,
    )
}

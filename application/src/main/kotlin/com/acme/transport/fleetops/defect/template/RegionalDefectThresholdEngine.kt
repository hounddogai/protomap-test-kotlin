package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional defect threshold calculation. */
interface RegionalDefectThresholdFactor {
    fun describe(): String
    fun adjust(item: RegionalDefectThreshold, value: Double): Double
}

data class CapRegionalDefectThresholdAdjuster(var cap: Double) : RegionalDefectThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDefectThreshold, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalDefectThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDefectThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDefectThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalDefectThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDefectThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDefectThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalDefectThresholdDiscountOffsetAdjuster(var weight: Double) : RegionalDefectThresholdFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: RegionalDefectThreshold, value: Double): Double = value + item.discount * weight
}

data class PercentRegionalDefectThresholdAdjuster(var percent: Double) : RegionalDefectThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDefectThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalDefectThresholdAdjuster(var step: Double) : RegionalDefectThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDefectThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the regional defect threshold steps in order and keeps a trace of each. */
data class RegionalDefectThresholdEngine(
    var steps: MutableList<RegionalDefectThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalDefectThreshold): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDefectThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDefectThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalDefectThresholdEngine(flags: Registry): RegionalDefectThresholdEngine {
    return RegionalDefectThresholdEngine(
        steps = mutableListOf<RegionalDefectThresholdFactor>(
            CapRegionalDefectThresholdAdjuster(29412.0),
            FlaggedRegionalDefectThresholdAdjuster(flags, "regional-defect-threshold-batched", 8.3),
            TieredRegionalDefectThresholdAdjuster(
                mutableListOf<Double>(4349.0, 4525.0),
                mutableListOf<Double>(0.83, 1.12),
            ),
            RegionalDefectThresholdDiscountOffsetAdjuster(0.81),
            PercentRegionalDefectThresholdAdjuster(1.3),
            RoundingRegionalDefectThresholdAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 98759.2,
    )
}

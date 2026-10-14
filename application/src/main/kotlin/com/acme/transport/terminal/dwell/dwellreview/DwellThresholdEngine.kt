package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.flags.Registry

/** One step of the dwell threshold calculation. */
interface DwellThresholdFactor {
    fun describe(): String
    fun adjust(item: DwellThreshold, value: Double): Double
}

data class PercentDwellThresholdAdjuster(var percent: Double) : DwellThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDwellThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDwellThresholdAdjuster(var cap: Double) : DwellThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredDwellThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelDwellThresholdAdjuster(var factors: MutableMap<String, Double>) : DwellThresholdFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: DwellThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DwellThresholdDiscountOffsetAdjuster(var weight: Double) : DwellThresholdFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: DwellThreshold, value: Double): Double = value + item.discount * weight
}

/** Runs the dwell threshold steps in order and keeps a trace of each. */
data class DwellThresholdEngine(
    var steps: MutableList<DwellThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellThreshold): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DwellThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellThresholdEngine(flags: Registry): DwellThresholdEngine {
    return DwellThresholdEngine(
        steps = mutableListOf<DwellThresholdFactor>(
            PercentDwellThresholdAdjuster(-18.6),
            FlaggedDwellThresholdAdjuster(flags, "dwell-threshold-primary", -7.1),
            CapDwellThresholdAdjuster(29430.0),
            TieredDwellThresholdAdjuster(
                mutableListOf<Double>(1744.0, 8059.0, 9319.0),
                mutableListOf<Double>(1.19, 1.05, 1.01),
            ),
            LabelDwellThresholdAdjuster(mutableMapOf<String, Double>("south" to 1.16)),
            DwellThresholdDiscountOffsetAdjuster(1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 27328.7,
    )
}

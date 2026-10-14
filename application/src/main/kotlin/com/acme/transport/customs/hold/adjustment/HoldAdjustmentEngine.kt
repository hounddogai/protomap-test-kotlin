package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hold adjustment calculation. */
interface HoldAdjustmentFactor {
    fun describe(): String
    fun adjust(item: HoldAdjustment, value: Double): Double
}

data class HoldAdjustmentWindowDaysOffsetAdjuster(var weight: Double) : HoldAdjustmentFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: HoldAdjustment, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class MinimumHoldAdjustmentAdjuster(var minimum: Double) : HoldAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredHoldAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingHoldAdjustmentAdjuster(var step: Double) : HoldAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedHoldAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelHoldAdjustmentAdjuster(var factors: MutableMap<String, Double>) : HoldAdjustmentFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: HoldAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold adjustment steps in order and keeps a trace of each. */
data class HoldAdjustmentEngine(
    var steps: MutableList<HoldAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HoldAdjustment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldAdjustmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHoldAdjustmentEngine(flags: Registry): HoldAdjustmentEngine {
    return HoldAdjustmentEngine(
        steps = mutableListOf<HoldAdjustmentFactor>(
            HoldAdjustmentWindowDaysOffsetAdjuster(1.85),
            MinimumHoldAdjustmentAdjuster(32.7),
            TieredHoldAdjustmentAdjuster(
                mutableListOf<Double>(504.0, 1260.0),
                mutableListOf<Double>(0.97, 1.17),
            ),
            RoundingHoldAdjustmentAdjuster(0.5),
            FlaggedHoldAdjustmentAdjuster(flags, "hold-adjustment-strict", -3.6),
            LabelHoldAdjustmentAdjuster(mutableMapOf<String, Double>("retail" to 1.11, "online" to 1.32)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 55855.0,
    )
}

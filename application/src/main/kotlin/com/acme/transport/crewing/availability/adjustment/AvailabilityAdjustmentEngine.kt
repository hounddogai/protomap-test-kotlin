package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the availability adjustment calculation. */
interface AvailabilityAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityAdjustment, value: Double): Double
}

data class AvailabilityAdjustmentEndsAtOffsetAdjuster(var weight: Double) : AvailabilityAdjustmentAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: AvailabilityAdjustment, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class TitleAvailabilityAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityAdjustmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AvailabilityAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAvailabilityAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAvailabilityAdjustmentAdjuster(var step: Double) : AvailabilityAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentAvailabilityAdjustmentAdjuster(var percent: Double) : AvailabilityAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the availability adjustment steps in order and keeps a trace of each. */
data class AvailabilityAdjustmentEngine(
    var steps: MutableList<AvailabilityAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityAdjustmentEngine(flags: Registry): AvailabilityAdjustmentEngine {
    return AvailabilityAdjustmentEngine(
        steps = mutableListOf<AvailabilityAdjustmentAdjuster>(
            AvailabilityAdjustmentEndsAtOffsetAdjuster(1.56),
            TitleAvailabilityAdjustmentAdjuster(mutableMapOf<String, Double>("north" to 0.77)),
            FlaggedAvailabilityAdjustmentAdjuster(flags, "availability-adjustment-rolling", -8.4),
            RoundingAvailabilityAdjustmentAdjuster(0.25),
            PercentAvailabilityAdjustmentAdjuster(-12.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 29027.1,
    )
}

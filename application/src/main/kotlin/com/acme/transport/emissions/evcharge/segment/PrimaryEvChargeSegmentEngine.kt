package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary ev charge segment calculation. */
interface PrimaryEvChargeSegmentFactor {
    fun describe(): String
    fun adjust(item: PrimaryEvChargeSegment, value: Double): Double
}

data class FlaggedPrimaryEvChargeSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryEvChargeSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryEvChargeSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelPrimaryEvChargeSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryEvChargeSegmentFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryEvChargeSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPrimaryEvChargeSegmentAdjuster(var step: Double) : PrimaryEvChargeSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEvChargeSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the primary ev charge segment steps in order and keeps a trace of each. */
data class PrimaryEvChargeSegmentEngine(
    var steps: MutableList<PrimaryEvChargeSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryEvChargeSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryEvChargeSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryEvChargeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryEvChargeSegmentEngine(flags: Registry): PrimaryEvChargeSegmentEngine {
    return PrimaryEvChargeSegmentEngine(
        steps = mutableListOf<PrimaryEvChargeSegmentFactor>(
            FlaggedPrimaryEvChargeSegmentAdjuster(flags, "primary-ev-charge-segment-global", 2.2),
            LabelPrimaryEvChargeSegmentAdjuster(mutableMapOf<String, Double>("south" to 1.31)),
            RoundingPrimaryEvChargeSegmentAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 19514.2,
    )
}

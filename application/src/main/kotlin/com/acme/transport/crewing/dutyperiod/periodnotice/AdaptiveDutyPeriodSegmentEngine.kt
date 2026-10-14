package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive duty period segment calculation. */
interface AdaptiveDutyPeriodSegmentStep {
    fun describe(): String
    fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double
}

data class MinimumAdaptiveDutyPeriodSegmentAdjuster(var minimum: Double) : AdaptiveDutyPeriodSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveDutyPeriodSegmentAdjuster(var cap: Double) : AdaptiveDutyPeriodSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveDutyPeriodSegmentAdjuster(var step: Double) : AdaptiveDutyPeriodSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double = roundTo(value, step)
}

data class AdaptiveDutyPeriodSegmentMarginOffsetAdjuster(var weight: Double) : AdaptiveDutyPeriodSegmentStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double = value + item.margin * weight
}

data class FlaggedAdaptiveDutyPeriodSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDutyPeriodSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveDutyPeriodSegmentAdjuster(var percent: Double) : AdaptiveDutyPeriodSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDutyPeriodSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive duty period segment steps in order and keeps a trace of each. */
data class AdaptiveDutyPeriodSegmentEngine(
    var steps: MutableList<AdaptiveDutyPeriodSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDutyPeriodSegment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDutyPeriodSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDutyPeriodSegmentEngine(flags: Registry): AdaptiveDutyPeriodSegmentEngine {
    return AdaptiveDutyPeriodSegmentEngine(
        steps = mutableListOf<AdaptiveDutyPeriodSegmentStep>(
            MinimumAdaptiveDutyPeriodSegmentAdjuster(23.3),
            CapAdaptiveDutyPeriodSegmentAdjuster(12426.0),
            RoundingAdaptiveDutyPeriodSegmentAdjuster(1.0),
            AdaptiveDutyPeriodSegmentMarginOffsetAdjuster(1.5),
            FlaggedAdaptiveDutyPeriodSegmentAdjuster(flags, "adaptive-duty-period-segment-pending", 5.7),
            PercentAdaptiveDutyPeriodSegmentAdjuster(-21.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 20457.3,
    )
}

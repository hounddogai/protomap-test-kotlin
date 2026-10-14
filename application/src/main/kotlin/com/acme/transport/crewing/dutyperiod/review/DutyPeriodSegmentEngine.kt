package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period segment calculation. */
interface DutyPeriodSegmentFactor {
    fun describe(): String
    fun adjust(item: DutyPeriodSegment, value: Double): Double
}

data class TieredDutyPeriodSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDutyPeriodSegmentAdjuster(var cap: Double) : DutyPeriodSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DutyPeriodSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedDutyPeriodSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDutyPeriodSegmentAdjuster(var step: Double) : DutyPeriodSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodSegment, value: Double): Double = roundTo(value, step)
}

data class DutyPeriodSegmentScoreOffsetAdjuster(var weight: Double) : DutyPeriodSegmentFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: DutyPeriodSegment, value: Double): Double = value + item.score * weight
}

data class MinimumDutyPeriodSegmentAdjuster(var minimum: Double) : DutyPeriodSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the duty period segment steps in order and keeps a trace of each. */
data class DutyPeriodSegmentEngine(
    var steps: MutableList<DutyPeriodSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodSegment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodSegmentEngine(flags: Registry): DutyPeriodSegmentEngine {
    return DutyPeriodSegmentEngine(
        steps = mutableListOf<DutyPeriodSegmentFactor>(
            TieredDutyPeriodSegmentAdjuster(
                mutableListOf<Double>(4151.0, 7352.0),
                mutableListOf<Double>(0.84, 0.94),
            ),
            CapDutyPeriodSegmentAdjuster(29771.0),
            FlaggedDutyPeriodSegmentAdjuster(flags, "duty-period-segment-rolling", 0.7),
            RoundingDutyPeriodSegmentAdjuster(0.5),
            DutyPeriodSegmentScoreOffsetAdjuster(0.36),
            MinimumDutyPeriodSegmentAdjuster(39.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 26717.3,
    )
}

package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling rate card segment calculation. */
interface RollingRateCardSegmentFactor {
    fun describe(): String
    fun adjust(item: RollingRateCardSegment, value: Double): Double
}

data class MinimumRollingRateCardSegmentAdjuster(var minimum: Double) : RollingRateCardSegmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRateCardSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingRateCardSegmentAdjuster(var cap: Double) : RollingRateCardSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRateCardSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingRateCardSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRateCardSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRateCardSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingRateCardSegmentThresholdOffsetAdjuster(var weight: Double) : RollingRateCardSegmentFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RollingRateCardSegment, value: Double): Double = value + item.threshold * weight
}

/** Runs the rolling rate card segment steps in order and keeps a trace of each. */
data class RollingRateCardSegmentEngine(
    var steps: MutableList<RollingRateCardSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingRateCardSegment): Double {
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

    fun evaluateAll(items: MutableList<RollingRateCardSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingRateCardSegmentEngine(flags: Registry): RollingRateCardSegmentEngine {
    return RollingRateCardSegmentEngine(
        steps = mutableListOf<RollingRateCardSegmentFactor>(
            MinimumRollingRateCardSegmentAdjuster(11.3),
            CapRollingRateCardSegmentAdjuster(28699.0),
            FlaggedRollingRateCardSegmentAdjuster(flags, "rolling-rate-card-segment-global", 7.3),
            RollingRateCardSegmentThresholdOffsetAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 25724.4,
    )
}

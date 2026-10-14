package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped locker segment calculation. */
interface CappedLockerSegmentModifier {
    fun describe(): String
    fun adjust(item: CappedLockerSegment, value: Double): Double
}

data class TieredCappedLockerSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLockerSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLockerSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedLockerSegmentAdjuster(var cap: Double) : CappedLockerSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedLockerSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedLockerSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedLockerSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedLockerSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCappedLockerSegmentAdjuster(var minimum: Double) : CappedLockerSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedLockerSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedLockerSegmentAdjuster(var percent: Double) : CappedLockerSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedLockerSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped locker segment steps in order and keeps a trace of each. */
data class CappedLockerSegmentEngine(
    var steps: MutableList<CappedLockerSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedLockerSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedLockerSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedLockerSegmentEngine(flags: Registry): CappedLockerSegmentEngine {
    return CappedLockerSegmentEngine(
        steps = mutableListOf<CappedLockerSegmentModifier>(
            TieredCappedLockerSegmentAdjuster(
                mutableListOf<Double>(1969.0, 7953.0),
                mutableListOf<Double>(0.84, 0.82),
            ),
            CapCappedLockerSegmentAdjuster(44457.0),
            FlaggedCappedLockerSegmentAdjuster(flags, "capped-locker-segment-global", 3.7),
            MinimumCappedLockerSegmentAdjuster(25.2),
            PercentCappedLockerSegmentAdjuster(-3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 62637.9,
    )
}

package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling near miss segment calculation. */
interface RollingNearMissSegmentModifier {
    fun describe(): String
    fun adjust(item: RollingNearMissSegment, value: Double): Double
}

data class CapRollingNearMissSegmentAdjuster(var cap: Double) : RollingNearMissSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingNearMissSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingNearMissSegmentAdjuster(var minimum: Double) : RollingNearMissSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingNearMissSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingNearMissSegmentAdjuster(var step: Double) : RollingNearMissSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingNearMissSegment, value: Double): Double = roundTo(value, step)
}

data class LabelRollingNearMissSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingNearMissSegmentModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingNearMissSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingNearMissSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingNearMissSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingNearMissSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling near miss segment steps in order and keeps a trace of each. */
data class RollingNearMissSegmentEngine(
    var steps: MutableList<RollingNearMissSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingNearMissSegment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingNearMissSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingNearMissSegmentEngine(): RollingNearMissSegmentEngine {
    return RollingNearMissSegmentEngine(
        steps = mutableListOf<RollingNearMissSegmentModifier>(
            CapRollingNearMissSegmentAdjuster(10813.0),
            MinimumRollingNearMissSegmentAdjuster(1.6),
            RoundingRollingNearMissSegmentAdjuster(0.1),
            LabelRollingNearMissSegmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.22, "north" to 0.7, "east" to 1.16),
            ),
            TieredRollingNearMissSegmentAdjuster(
                mutableListOf<Double>(3543.0, 3958.0),
                mutableListOf<Double>(1.08, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 75372.7,
    )
}

package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the coaching adjustment calculation. */
interface CoachingAdjustmentFactor {
    fun describe(): String
    fun adjust(item: CoachingAdjustment, value: Double): Double
}

data class CapCoachingAdjustmentAdjuster(var cap: Double) : CoachingAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedCoachingAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCoachingAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCoachingAdjustmentAdjuster(var minimum: Double) : CoachingAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the coaching adjustment steps in order and keeps a trace of each. */
data class CoachingAdjustmentEngine(
    var steps: MutableList<CoachingAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CoachingAdjustment): Double {
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

    fun evaluateAll(items: MutableList<CoachingAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCoachingAdjustmentEngine(flags: Registry): CoachingAdjustmentEngine {
    return CoachingAdjustmentEngine(
        steps = mutableListOf<CoachingAdjustmentFactor>(
            CapCoachingAdjustmentAdjuster(46129.0),
            FlaggedCoachingAdjustmentAdjuster(flags, "coaching-adjustment-seasonal", -8.0),
            TieredCoachingAdjustmentAdjuster(
                mutableListOf<Double>(3393.0, 6197.0, 8431.0, 8702.0),
                mutableListOf<Double>(1.11, 0.89, 0.88, 1.02),
            ),
            MinimumCoachingAdjustmentAdjuster(25.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 85807.2,
    )
}

package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the coaching window calculation. */
interface CoachingWindowAdjuster {
    fun describe(): String
    fun adjust(item: CoachingWindow, value: Double): Double
}

data class FlaggedCoachingWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCoachingWindowAdjuster(var percent: Double) : CoachingWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCoachingWindowAdjuster(var cap: Double) : CoachingWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingWindow, value: Double): Double = minOf(value, cap)
}

data class TieredCoachingWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CoachingWindowThresholdOffsetAdjuster(var weight: Double) : CoachingWindowAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: CoachingWindow, value: Double): Double = value + item.threshold * weight
}

/** Runs the coaching window steps in order and keeps a trace of each. */
data class CoachingWindowEngine(
    var steps: MutableList<CoachingWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingWindow): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingWindowEngine(flags: Registry): CoachingWindowEngine {
    return CoachingWindowEngine(
        steps = mutableListOf<CoachingWindowAdjuster>(
            FlaggedCoachingWindowAdjuster(flags, "coaching-window-tiered", -5.9),
            PercentCoachingWindowAdjuster(24.8),
            CapCoachingWindowAdjuster(40081.0),
            TieredCoachingWindowAdjuster(
                mutableListOf<Double>(1582.0, 6075.0, 6166.0, 9393.0),
                mutableListOf<Double>(1.0, 0.91, 1.15, 1.08),
            ),
            CoachingWindowThresholdOffsetAdjuster(1.26),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 18298.5,
    )
}

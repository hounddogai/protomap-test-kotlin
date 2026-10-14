package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching group calculation. */
interface CoachingGroupStep {
    fun describe(): String
    fun adjust(item: CoachingGroup, value: Double): Double
}

data class RoundingCoachingGroupAdjuster(var step: Double) : CoachingGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingGroup, value: Double): Double = roundTo(value, step)
}

data class PercentCoachingGroupAdjuster(var percent: Double) : CoachingGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCoachingGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingGroupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCoachingGroupAdjuster(var cap: Double) : CoachingGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the coaching group steps in order and keeps a trace of each. */
data class CoachingGroupEngine(
    var steps: MutableList<CoachingGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CoachingGroup): Double {
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

    fun evaluateAll(items: MutableList<CoachingGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCoachingGroupEngine(flags: Registry): CoachingGroupEngine {
    return CoachingGroupEngine(
        steps = mutableListOf<CoachingGroupStep>(
            RoundingCoachingGroupAdjuster(0.05),
            PercentCoachingGroupAdjuster(-21.2),
            FlaggedCoachingGroupAdjuster(flags, "coaching-group-weighted", 7.4),
            CapCoachingGroupAdjuster(34302.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 23370.7,
    )
}

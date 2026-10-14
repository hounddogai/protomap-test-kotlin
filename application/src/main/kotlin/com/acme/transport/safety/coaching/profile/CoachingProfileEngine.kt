package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching profile calculation. */
interface CoachingProfileFactor {
    fun describe(): String
    fun adjust(item: CoachingProfile, value: Double): Double
}

data class MinimumCoachingProfileAdjuster(var minimum: Double) : CoachingProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCoachingProfileAdjuster(var step: Double) : CoachingProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingProfile, value: Double): Double = roundTo(value, step)
}

data class FlaggedCoachingProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching profile steps in order and keeps a trace of each. */
data class CoachingProfileEngine(
    var steps: MutableList<CoachingProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingProfile): Double {
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

    fun evaluateAll(items: MutableList<CoachingProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingProfileEngine(flags: Registry): CoachingProfileEngine {
    return CoachingProfileEngine(
        steps = mutableListOf<CoachingProfileFactor>(
            MinimumCoachingProfileAdjuster(41.9),
            RoundingCoachingProfileAdjuster(0.5),
            FlaggedCoachingProfileAdjuster(flags, "coaching-profile-capped", 7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 92805.3,
    )
}

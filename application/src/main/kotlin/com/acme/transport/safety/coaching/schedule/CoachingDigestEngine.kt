package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the coaching digest calculation. */
interface CoachingDigestFactor {
    fun describe(): String
    fun adjust(item: CoachingDigest, value: Double): Double
}

data class CoachingDigestMarginOffsetAdjuster(var weight: Double) : CoachingDigestFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: CoachingDigest, value: Double): Double = value + item.margin * weight
}

data class TitleCoachingDigestAdjuster(var factors: MutableMap<String, Double>) : CoachingDigestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: CoachingDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCoachingDigestAdjuster(var minimum: Double) : CoachingDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCoachingDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching digest steps in order and keeps a trace of each. */
data class CoachingDigestEngine(
    var steps: MutableList<CoachingDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingDigest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingDigestEngine(flags: Registry): CoachingDigestEngine {
    return CoachingDigestEngine(
        steps = mutableListOf<CoachingDigestFactor>(
            CoachingDigestMarginOffsetAdjuster(1.35),
            TitleCoachingDigestAdjuster(mutableMapOf<String, Double>("retail" to 0.85, "west" to 0.76)),
            MinimumCoachingDigestAdjuster(7.0),
            FlaggedCoachingDigestAdjuster(flags, "coaching-digest-capped", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 99212.4,
    )
}

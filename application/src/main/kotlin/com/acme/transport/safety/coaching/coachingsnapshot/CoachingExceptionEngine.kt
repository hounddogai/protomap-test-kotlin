package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching exception calculation. */
interface CoachingExceptionFactor {
    fun describe(): String
    fun adjust(item: CoachingException, value: Double): Double
}

data class RegionCoachingExceptionAdjuster(var factors: MutableMap<String, Double>) : CoachingExceptionFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: CoachingException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCoachingExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCoachingExceptionAdjuster(var step: Double) : CoachingExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingException, value: Double): Double = roundTo(value, step)
}

data class FlaggedCoachingExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching exception steps in order and keeps a trace of each. */
data class CoachingExceptionEngine(
    var steps: MutableList<CoachingExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingException): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingExceptionEngine(flags: Registry): CoachingExceptionEngine {
    return CoachingExceptionEngine(
        steps = mutableListOf<CoachingExceptionFactor>(
            RegionCoachingExceptionAdjuster(
                mutableMapOf<String, Double>("online" to 0.95, "north" to 1.02, "central" to 0.73),
            ),
            TieredCoachingExceptionAdjuster(
                mutableListOf<Double>(1567.0, 4261.0, 5624.0, 9372.0),
                mutableListOf<Double>(0.84, 0.97, 1.15, 0.84),
            ),
            RoundingCoachingExceptionAdjuster(1.0),
            FlaggedCoachingExceptionAdjuster(flags, "coaching-exception-manual", -1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 27911.9,
    )
}

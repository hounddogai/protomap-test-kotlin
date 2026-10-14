package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.flags.Registry

/** One step of the coaching cycle calculation. */
interface CoachingCycleModifier {
    fun describe(): String
    fun adjust(item: CoachingCycle, value: Double): Double
}

data class MinimumCoachingCycleAdjuster(var minimum: Double) : CoachingCycleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCoachingCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCoachingCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitCoachingCycleAdjuster(var factors: MutableMap<String, Double>) : CoachingCycleModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: CoachingCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the coaching cycle steps in order and keeps a trace of each. */
data class CoachingCycleEngine(
    var steps: MutableList<CoachingCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CoachingCycle): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCoachingCycleEngine(flags: Registry): CoachingCycleEngine {
    return CoachingCycleEngine(
        steps = mutableListOf<CoachingCycleModifier>(
            MinimumCoachingCycleAdjuster(9.5),
            FlaggedCoachingCycleAdjuster(flags, "coaching-cycle-default", -3.0),
            TieredCoachingCycleAdjuster(
                mutableListOf<Double>(2128.0, 9761.0, 9888.0),
                mutableListOf<Double>(1.1, 1.1, 0.93),
            ),
            UnitCoachingCycleAdjuster(
                mutableMapOf<String, Double>("online" to 1.18, "retail" to 1.28, "central" to 1.29),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 49260.8,
    )
}

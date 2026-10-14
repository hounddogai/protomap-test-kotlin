package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling coaching score calculation. */
interface RollingCoachingScoreModifier {
    fun describe(): String
    fun adjust(item: RollingCoachingScore, value: Double): Double
}

data class PercentRollingCoachingScoreAdjuster(var percent: Double) : RollingCoachingScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCoachingScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingCoachingScoreAdjuster(var step: Double) : RollingCoachingScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCoachingScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingCoachingScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCoachingScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCoachingScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRollingCoachingScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCoachingScoreModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingCoachingScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling coaching score steps in order and keeps a trace of each. */
data class RollingCoachingScoreEngine(
    var steps: MutableList<RollingCoachingScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingCoachingScore): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingCoachingScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingCoachingScoreEngine(flags: Registry): RollingCoachingScoreEngine {
    return RollingCoachingScoreEngine(
        steps = mutableListOf<RollingCoachingScoreModifier>(
            PercentRollingCoachingScoreAdjuster(20.2),
            RoundingRollingCoachingScoreAdjuster(0.5),
            FlaggedRollingCoachingScoreAdjuster(flags, "rolling-coaching-score-fallback", -7.5),
            NameRollingCoachingScoreAdjuster(mutableMapOf<String, Double>("south" to 1.0)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 10686.9,
    )
}

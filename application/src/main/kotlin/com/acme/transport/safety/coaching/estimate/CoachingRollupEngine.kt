package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching rollup calculation. */
interface CoachingRollupAdjuster {
    fun describe(): String
    fun adjust(item: CoachingRollup, value: Double): Double
}

data class TieredCoachingRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCoachingRollupAdjuster(var step: Double) : CoachingRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingRollup, value: Double): Double = roundTo(value, step)
}

data class PercentCoachingRollupAdjuster(var percent: Double) : CoachingRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCoachingRollupAdjuster(var cap: Double) : CoachingRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingRollup, value: Double): Double = minOf(value, cap)
}

data class FlaggedCoachingRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching rollup steps in order and keeps a trace of each. */
data class CoachingRollupEngine(
    var steps: MutableList<CoachingRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CoachingRollup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCoachingRollupEngine(flags: Registry): CoachingRollupEngine {
    return CoachingRollupEngine(
        steps = mutableListOf<CoachingRollupAdjuster>(
            TieredCoachingRollupAdjuster(
                mutableListOf<Double>(6177.0, 6323.0, 6787.0),
                mutableListOf<Double>(0.8, 1.19, 0.93),
            ),
            RoundingCoachingRollupAdjuster(0.5),
            PercentCoachingRollupAdjuster(-21.4),
            CapCoachingRollupAdjuster(16317.0),
            FlaggedCoachingRollupAdjuster(flags, "coaching-rollup-regional", 5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 95051.5,
    )
}

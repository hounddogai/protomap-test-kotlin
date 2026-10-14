package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling locker estimate calculation. */
interface RollingLockerEstimateStep {
    fun describe(): String
    fun adjust(item: RollingLockerEstimate, value: Double): Double
}

data class RollingLockerEstimateAttemptsOffsetAdjuster(var weight: Double) : RollingLockerEstimateStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RollingLockerEstimate, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class TieredRollingLockerEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingLockerEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingLockerEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingLockerEstimateAdjuster(var step: Double) : RollingLockerEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingLockerEstimate, value: Double): Double = roundTo(value, step)
}

data class LaneRollingLockerEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingLockerEstimateStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: RollingLockerEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling locker estimate steps in order and keeps a trace of each. */
data class RollingLockerEstimateEngine(
    var steps: MutableList<RollingLockerEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingLockerEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingLockerEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingLockerEstimateEngine(): RollingLockerEstimateEngine {
    return RollingLockerEstimateEngine(
        steps = mutableListOf<RollingLockerEstimateStep>(
            RollingLockerEstimateAttemptsOffsetAdjuster(1.91),
            TieredRollingLockerEstimateAdjuster(
                mutableListOf<Double>(2861.0, 5541.0, 6350.0),
                mutableListOf<Double>(0.86, 1.18, 0.88),
            ),
            RoundingRollingLockerEstimateAdjuster(0.5),
            LaneRollingLockerEstimateAdjuster(
                mutableMapOf<String, Double>("central" to 1.06, "east" to 0.98, "retail" to 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 46222.2,
    )
}

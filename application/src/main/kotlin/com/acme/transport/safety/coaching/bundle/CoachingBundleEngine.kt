package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching bundle calculation. */
interface CoachingBundleFactor {
    fun describe(): String
    fun adjust(item: CoachingBundle, value: Double): Double
}

data class CoachingBundleWeightOffsetAdjuster(var weight: Double) : CoachingBundleFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: CoachingBundle, value: Double): Double = value + item.weight * weight
}

data class TieredCoachingBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCoachingBundleAdjuster(var step: Double) : CoachingBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingBundle, value: Double): Double = roundTo(value, step)
}

data class PercentCoachingBundleAdjuster(var percent: Double) : CoachingBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the coaching bundle steps in order and keeps a trace of each. */
data class CoachingBundleEngine(
    var steps: MutableList<CoachingBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CoachingBundle): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCoachingBundleEngine(): CoachingBundleEngine {
    return CoachingBundleEngine(
        steps = mutableListOf<CoachingBundleFactor>(
            CoachingBundleWeightOffsetAdjuster(1.38),
            TieredCoachingBundleAdjuster(
                mutableListOf<Double>(5562.0, 7555.0),
                mutableListOf<Double>(0.83, 0.97),
            ),
            RoundingCoachingBundleAdjuster(0.25),
            PercentCoachingBundleAdjuster(23.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 82022.0,
    )
}

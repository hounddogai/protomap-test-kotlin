package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss review calculation. */
interface NearMissReviewFactor {
    fun describe(): String
    fun adjust(item: NearMissReview, value: Double): Double
}

data class CapNearMissReviewAdjuster(var cap: Double) : NearMissReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissReview, value: Double): Double = minOf(value, cap)
}

data class MinimumNearMissReviewAdjuster(var minimum: Double) : NearMissReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredNearMissReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingNearMissReviewAdjuster(var step: Double) : NearMissReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissReview, value: Double): Double = roundTo(value, step)
}

/** Runs the near miss review steps in order and keeps a trace of each. */
data class NearMissReviewEngine(
    var steps: MutableList<NearMissReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissReview): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissReviewEngine(): NearMissReviewEngine {
    return NearMissReviewEngine(
        steps = mutableListOf<NearMissReviewFactor>(
            CapNearMissReviewAdjuster(24484.0),
            MinimumNearMissReviewAdjuster(23.8),
            TieredNearMissReviewAdjuster(
                mutableListOf<Double>(1204.0, 9316.0),
                mutableListOf<Double>(0.82, 1.06),
            ),
            RoundingNearMissReviewAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 67980.6,
    )
}

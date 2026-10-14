package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay review calculation. */
interface RelayReviewStep {
    fun describe(): String
    fun adjust(item: RelayReview, value: Double): Double
}

data class RoundingRelayReviewAdjuster(var step: Double) : RelayReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayReview, value: Double): Double = roundTo(value, step)
}

data class PercentRelayReviewAdjuster(var percent: Double) : RelayReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionRelayReviewAdjuster(var factors: MutableMap<String, Double>) : RelayReviewStep {
    override fun describe(): String = "by region"

    override fun adjust(item: RelayReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRelayReviewAdjuster(var cap: Double) : RelayReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayReview, value: Double): Double = minOf(value, cap)
}

data class TieredRelayReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RelayReviewCeilingOffsetAdjuster(var weight: Double) : RelayReviewStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RelayReview, value: Double): Double = value + item.ceiling * weight
}

/** Runs the relay review steps in order and keeps a trace of each. */
data class RelayReviewEngine(
    var steps: MutableList<RelayReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RelayReview): Double {
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

    fun evaluateAll(items: MutableList<RelayReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRelayReviewEngine(): RelayReviewEngine {
    return RelayReviewEngine(
        steps = mutableListOf<RelayReviewStep>(
            RoundingRelayReviewAdjuster(0.05),
            PercentRelayReviewAdjuster(0.3),
            RegionRelayReviewAdjuster(mutableMapOf<String, Double>("north" to 1.3, "online" to 1.36)),
            CapRelayReviewAdjuster(38660.0),
            TieredRelayReviewAdjuster(
                mutableListOf<Double>(3416.0, 4329.0, 8471.0, 8735.0),
                mutableListOf<Double>(0.86, 0.81, 1.16, 1.07),
            ),
            RelayReviewCeilingOffsetAdjuster(0.63),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 3292.3,
    )
}

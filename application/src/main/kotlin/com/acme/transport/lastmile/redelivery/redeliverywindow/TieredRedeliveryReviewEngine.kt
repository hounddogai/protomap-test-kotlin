package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered redelivery review calculation. */
interface TieredRedeliveryReviewStep {
    fun describe(): String
    fun adjust(item: TieredRedeliveryReview, value: Double): Double
}

data class CapTieredRedeliveryReviewAdjuster(var cap: Double) : TieredRedeliveryReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredRedeliveryReview, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredRedeliveryReviewAdjuster(var minimum: Double) : TieredRedeliveryReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRedeliveryReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentTieredRedeliveryReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRedeliveryReviewStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredRedeliveryReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered redelivery review steps in order and keeps a trace of each. */
data class TieredRedeliveryReviewEngine(
    var steps: MutableList<TieredRedeliveryReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRedeliveryReview): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRedeliveryReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRedeliveryReviewEngine(): TieredRedeliveryReviewEngine {
    return TieredRedeliveryReviewEngine(
        steps = mutableListOf<TieredRedeliveryReviewStep>(
            CapTieredRedeliveryReviewAdjuster(40052.0),
            MinimumTieredRedeliveryReviewAdjuster(1.8),
            SegmentTieredRedeliveryReviewAdjuster(
                mutableMapOf<String, Double>("west" to 1.07, "online" to 1.26, "central" to 0.73),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 30192.3,
    )
}

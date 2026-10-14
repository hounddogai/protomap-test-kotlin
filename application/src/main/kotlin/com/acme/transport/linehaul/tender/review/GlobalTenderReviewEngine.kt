package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global tender review calculation. */
interface GlobalTenderReviewModifier {
    fun describe(): String
    fun adjust(item: GlobalTenderReview, value: Double): Double
}

data class GlobalTenderReviewVersionOffsetAdjuster(var weight: Double) : GlobalTenderReviewModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: GlobalTenderReview, value: Double): Double = value + item.version.toDouble() * weight
}

data class MinimumGlobalTenderReviewAdjuster(var minimum: Double) : GlobalTenderReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTenderReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitGlobalTenderReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTenderReviewModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: GlobalTenderReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalTenderReviewAdjuster(var cap: Double) : GlobalTenderReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalTenderReview, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalTenderReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTenderReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTenderReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global tender review steps in order and keeps a trace of each. */
data class GlobalTenderReviewEngine(
    var steps: MutableList<GlobalTenderReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalTenderReview): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTenderReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalTenderReviewEngine(): GlobalTenderReviewEngine {
    return GlobalTenderReviewEngine(
        steps = mutableListOf<GlobalTenderReviewModifier>(
            GlobalTenderReviewVersionOffsetAdjuster(0.12),
            MinimumGlobalTenderReviewAdjuster(18.1),
            UnitGlobalTenderReviewAdjuster(mutableMapOf<String, Double>("north" to 0.88)),
            CapGlobalTenderReviewAdjuster(38044.0),
            TieredGlobalTenderReviewAdjuster(
                mutableListOf<Double>(1084.0, 1607.0),
                mutableListOf<Double>(0.98, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 96894.4,
    )
}

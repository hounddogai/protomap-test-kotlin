package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period review calculation. */
interface DutyPeriodReviewModifier {
    fun describe(): String
    fun adjust(item: DutyPeriodReview, value: Double): Double
}

data class DutyPeriodReviewBudgetOffsetAdjuster(var weight: Double) : DutyPeriodReviewModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: DutyPeriodReview, value: Double): Double = value + item.budget * weight
}

data class PercentDutyPeriodReviewAdjuster(var percent: Double) : DutyPeriodReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDutyPeriodReviewAdjuster(var step: Double) : DutyPeriodReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodReview, value: Double): Double = roundTo(value, step)
}

data class CountryDutyPeriodReviewAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodReviewModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: DutyPeriodReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDutyPeriodReviewAdjuster(var minimum: Double) : DutyPeriodReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDutyPeriodReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the duty period review steps in order and keeps a trace of each. */
data class DutyPeriodReviewEngine(
    var steps: MutableList<DutyPeriodReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodReview): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodReviewEngine(): DutyPeriodReviewEngine {
    return DutyPeriodReviewEngine(
        steps = mutableListOf<DutyPeriodReviewModifier>(
            DutyPeriodReviewBudgetOffsetAdjuster(0.24),
            PercentDutyPeriodReviewAdjuster(-24.3),
            RoundingDutyPeriodReviewAdjuster(5.0),
            CountryDutyPeriodReviewAdjuster(mutableMapOf<String, Double>("west" to 1.28)),
            MinimumDutyPeriodReviewAdjuster(16.2),
            TieredDutyPeriodReviewAdjuster(
                mutableListOf<Double>(1012.0, 1140.0, 6075.0, 8175.0),
                mutableListOf<Double>(1.02, 0.84, 1.14, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 25947.0,
    )
}

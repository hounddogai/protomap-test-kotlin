package com.acme.transport.lastmile.locker.target

/** One step of the locker review calculation. */
interface LockerReviewAdjuster {
    fun describe(): String
    fun adjust(item: LockerReview, value: Double): Double
}

data class LockerReviewBatchSizeOffsetAdjuster(var weight: Double) : LockerReviewAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LockerReview, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class TieredLockerReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLockerReviewAdjuster(var minimum: Double) : LockerReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the locker review steps in order and keeps a trace of each. */
data class LockerReviewEngine(
    var steps: MutableList<LockerReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerReview): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LockerReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerReviewEngine(): LockerReviewEngine {
    return LockerReviewEngine(
        steps = mutableListOf<LockerReviewAdjuster>(
            LockerReviewBatchSizeOffsetAdjuster(1.8),
            TieredLockerReviewAdjuster(
                mutableListOf<Double>(5023.0, 5311.0, 6379.0, 7879.0),
                mutableListOf<Double>(1.0, 1.1, 1.17, 1.09),
            ),
            MinimumLockerReviewAdjuster(3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 84833.4,
    )
}

package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge review calculation. */
interface EvChargeReviewFactor {
    fun describe(): String
    fun adjust(item: EvChargeReview, value: Double): Double
}

data class TieredEvChargeReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedEvChargeReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumEvChargeReviewAdjuster(var minimum: Double) : EvChargeReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameEvChargeReviewAdjuster(var factors: MutableMap<String, Double>) : EvChargeReviewFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: EvChargeReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the ev charge review steps in order and keeps a trace of each. */
data class EvChargeReviewEngine(
    var steps: MutableList<EvChargeReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeReview): Double {
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

    fun evaluateAll(items: MutableList<EvChargeReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeReviewEngine(flags: Registry): EvChargeReviewEngine {
    return EvChargeReviewEngine(
        steps = mutableListOf<EvChargeReviewFactor>(
            TieredEvChargeReviewAdjuster(
                mutableListOf<Double>(2945.0, 4037.0, 6221.0, 6228.0),
                mutableListOf<Double>(1.1, 0.83, 0.99, 0.99),
            ),
            FlaggedEvChargeReviewAdjuster(flags, "ev-charge-review-default", -8.8),
            MinimumEvChargeReviewAdjuster(2.2),
            NameEvChargeReviewAdjuster(
                mutableMapOf<String, Double>("online" to 0.93, "north" to 1.22, "west" to 0.78),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 42351.1,
    )
}

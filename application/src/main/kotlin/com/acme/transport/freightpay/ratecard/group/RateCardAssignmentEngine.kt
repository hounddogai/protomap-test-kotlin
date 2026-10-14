package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card assignment calculation. */
interface RateCardAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: RateCardAssignment, value: Double): Double
}

data class TieredRateCardAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRateCardAssignmentAdjuster(var percent: Double) : RateCardAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleRateCardAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RateCardAssignmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RateCardAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRateCardAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rate card assignment steps in order and keeps a trace of each. */
data class RateCardAssignmentEngine(
    var steps: MutableList<RateCardAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardAssignment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardAssignmentEngine(flags: Registry): RateCardAssignmentEngine {
    return RateCardAssignmentEngine(
        steps = mutableListOf<RateCardAssignmentAdjuster>(
            TieredRateCardAssignmentAdjuster(
                mutableListOf<Double>(5360.0, 7913.0),
                mutableListOf<Double>(1.19, 0.87),
            ),
            PercentRateCardAssignmentAdjuster(7.1),
            TitleRateCardAssignmentAdjuster(mutableMapOf<String, Double>("central" to 0.87)),
            FlaggedRateCardAssignmentAdjuster(flags, "rate-card-assignment-default", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 53196.2,
    )
}

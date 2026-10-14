package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card record calculation. */
interface RateCardRecordModifier {
    fun describe(): String
    fun adjust(item: RateCardRecord, value: Double): Double
}

data class PercentRateCardRecordAdjuster(var percent: Double) : RateCardRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRateCardRecordAdjuster(var minimum: Double) : RateCardRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RateCardRecordRateOffsetAdjuster(var weight: Double) : RateCardRecordModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RateCardRecord, value: Double): Double = value + item.rate * weight
}

data class RegionRateCardRecordAdjuster(var factors: MutableMap<String, Double>) : RateCardRecordModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: RateCardRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rate card record steps in order and keeps a trace of each. */
data class RateCardRecordEngine(
    var steps: MutableList<RateCardRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RateCardRecord): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRateCardRecordEngine(): RateCardRecordEngine {
    return RateCardRecordEngine(
        steps = mutableListOf<RateCardRecordModifier>(
            PercentRateCardRecordAdjuster(-24.6),
            MinimumRateCardRecordAdjuster(46.0),
            RateCardRecordRateOffsetAdjuster(0.52),
            RegionRateCardRecordAdjuster(
                mutableMapOf<String, Double>("west" to 0.99, "retail" to 0.82, "central" to 1.21),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 38539.6,
    )
}

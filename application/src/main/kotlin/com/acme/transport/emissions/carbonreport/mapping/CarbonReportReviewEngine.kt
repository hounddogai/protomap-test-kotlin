package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report review calculation. */
interface CarbonReportReviewStep {
    fun describe(): String
    fun adjust(item: CarbonReportReview, value: Double): Double
}

data class ChannelCarbonReportReviewAdjuster(var factors: MutableMap<String, Double>) : CarbonReportReviewStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: CarbonReportReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCarbonReportReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCarbonReportReviewAdjuster(var cap: Double) : CarbonReportReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportReview, value: Double): Double = minOf(value, cap)
}

/** Runs the carbon report review steps in order and keeps a trace of each. */
data class CarbonReportReviewEngine(
    var steps: MutableList<CarbonReportReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportReview): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportReviewEngine(): CarbonReportReviewEngine {
    return CarbonReportReviewEngine(
        steps = mutableListOf<CarbonReportReviewStep>(
            ChannelCarbonReportReviewAdjuster(mutableMapOf<String, Double>("south" to 0.74, "online" to 0.84)),
            TieredCarbonReportReviewAdjuster(
                mutableListOf<Double>(4774.0, 8612.0, 8825.0),
                mutableListOf<Double>(1.01, 0.99, 0.93),
            ),
            CapCarbonReportReviewAdjuster(25604.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 24287.0,
    )
}

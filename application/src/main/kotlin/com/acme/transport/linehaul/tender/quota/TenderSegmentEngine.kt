package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender segment calculation. */
interface TenderSegmentAdjuster {
    fun describe(): String
    fun adjust(item: TenderSegment, value: Double): Double
}

data class PercentTenderSegmentAdjuster(var percent: Double) : TenderSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TenderSegmentSequenceOffsetAdjuster(var weight: Double) : TenderSegmentAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TenderSegment, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredTenderSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTenderSegmentAdjuster(var cap: Double) : TenderSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderSegment, value: Double): Double = minOf(value, cap)
}

data class MinimumTenderSegmentAdjuster(var minimum: Double) : TenderSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tender segment steps in order and keeps a trace of each. */
data class TenderSegmentEngine(
    var steps: MutableList<TenderSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TenderSegment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTenderSegmentEngine(): TenderSegmentEngine {
    return TenderSegmentEngine(
        steps = mutableListOf<TenderSegmentAdjuster>(
            PercentTenderSegmentAdjuster(-2.7),
            TenderSegmentSequenceOffsetAdjuster(1.29),
            TieredTenderSegmentAdjuster(
                mutableListOf<Double>(4373.0, 5957.0, 9970.0),
                mutableListOf<Double>(1.16, 0.87, 1.18),
            ),
            CapTenderSegmentAdjuster(25191.0),
            MinimumTenderSegmentAdjuster(24.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 16072.0,
    )
}

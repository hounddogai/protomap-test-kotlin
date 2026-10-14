package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker segment calculation. */
interface BrokerSegmentAdjuster {
    fun describe(): String
    fun adjust(item: BrokerSegment, value: Double): Double
}

data class RegionBrokerSegmentAdjuster(var factors: MutableMap<String, Double>) : BrokerSegmentAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: BrokerSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BrokerSegmentDiscountOffsetAdjuster(var weight: Double) : BrokerSegmentAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BrokerSegment, value: Double): Double = value + item.discount * weight
}

data class RoundingBrokerSegmentAdjuster(var step: Double) : BrokerSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerSegment, value: Double): Double = roundTo(value, step)
}

data class TieredBrokerSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBrokerSegmentAdjuster(var percent: Double) : BrokerSegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker segment steps in order and keeps a trace of each. */
data class BrokerSegmentEngine(
    var steps: MutableList<BrokerSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerSegment): Double {
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

    fun evaluateAll(items: MutableList<BrokerSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerSegmentEngine(): BrokerSegmentEngine {
    return BrokerSegmentEngine(
        steps = mutableListOf<BrokerSegmentAdjuster>(
            RegionBrokerSegmentAdjuster(mutableMapOf<String, Double>("south" to 1.3, "east" to 0.93)),
            BrokerSegmentDiscountOffsetAdjuster(1.49),
            RoundingBrokerSegmentAdjuster(0.25),
            TieredBrokerSegmentAdjuster(
                mutableListOf<Double>(555.0, 2465.0, 3379.0, 8012.0),
                mutableListOf<Double>(0.82, 0.99, 0.87, 0.85),
            ),
            PercentBrokerSegmentAdjuster(-8.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 45929.1,
    )
}

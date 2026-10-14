package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker group calculation. */
interface BrokerGroupAdjuster {
    fun describe(): String
    fun adjust(item: BrokerGroup, value: Double): Double
}

data class BrokerGroupDistanceKmOffsetAdjuster(var weight: Double) : BrokerGroupAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: BrokerGroup, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingBrokerGroupAdjuster(var step: Double) : BrokerGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerGroup, value: Double): Double = roundTo(value, step)
}

data class PercentBrokerGroupAdjuster(var percent: Double) : BrokerGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBrokerGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBrokerGroupAdjuster(var cap: Double) : BrokerGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the broker group steps in order and keeps a trace of each. */
data class BrokerGroupEngine(
    var steps: MutableList<BrokerGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BrokerGroup): Double {
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

    fun evaluateAll(items: MutableList<BrokerGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBrokerGroupEngine(): BrokerGroupEngine {
    return BrokerGroupEngine(
        steps = mutableListOf<BrokerGroupAdjuster>(
            BrokerGroupDistanceKmOffsetAdjuster(1.16),
            RoundingBrokerGroupAdjuster(0.05),
            PercentBrokerGroupAdjuster(-20.1),
            TieredBrokerGroupAdjuster(
                mutableListOf<Double>(3096.0, 5279.0, 6314.0, 8346.0),
                mutableListOf<Double>(1.18, 0.87, 0.88, 0.85),
            ),
            CapBrokerGroupAdjuster(862.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 98363.0,
    )
}

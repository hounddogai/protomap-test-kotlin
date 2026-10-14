package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered broker request calculation. */
interface TieredBrokerRequestModifier {
    fun describe(): String
    fun adjust(item: TieredBrokerRequest, value: Double): Double
}

data class TieredBrokerRequestQuantityOffsetAdjuster(var weight: Double) : TieredBrokerRequestModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TieredBrokerRequest, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapTieredBrokerRequestAdjuster(var cap: Double) : TieredBrokerRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredBrokerRequest, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredBrokerRequestAdjuster(var step: Double) : TieredBrokerRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredBrokerRequest, value: Double): Double = roundTo(value, step)
}

data class TieredTieredBrokerRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredBrokerRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredBrokerRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered broker request steps in order and keeps a trace of each. */
data class TieredBrokerRequestEngine(
    var steps: MutableList<TieredBrokerRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredBrokerRequest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredBrokerRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredBrokerRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredBrokerRequestEngine(): TieredBrokerRequestEngine {
    return TieredBrokerRequestEngine(
        steps = mutableListOf<TieredBrokerRequestModifier>(
            TieredBrokerRequestQuantityOffsetAdjuster(1.26),
            CapTieredBrokerRequestAdjuster(30004.0),
            RoundingTieredBrokerRequestAdjuster(5.0),
            TieredTieredBrokerRequestAdjuster(
                mutableListOf<Double>(6717.0, 9771.0),
                mutableListOf<Double>(1.17, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 97239.6,
    )
}

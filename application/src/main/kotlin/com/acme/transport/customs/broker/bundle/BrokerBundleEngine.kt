package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker bundle calculation. */
interface BrokerBundleFactor {
    fun describe(): String
    fun adjust(item: BrokerBundle, value: Double): Double
}

data class RoundingBrokerBundleAdjuster(var step: Double) : BrokerBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerBundle, value: Double): Double = roundTo(value, step)
}

data class PercentBrokerBundleAdjuster(var percent: Double) : BrokerBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BrokerBundleFuelLitersOffsetAdjuster(var weight: Double) : BrokerBundleFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: BrokerBundle, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the broker bundle steps in order and keeps a trace of each. */
data class BrokerBundleEngine(
    var steps: MutableList<BrokerBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerBundle): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerBundleEngine(): BrokerBundleEngine {
    return BrokerBundleEngine(
        steps = mutableListOf<BrokerBundleFactor>(
            RoundingBrokerBundleAdjuster(0.05),
            PercentBrokerBundleAdjuster(-24.2),
            BrokerBundleFuelLitersOffsetAdjuster(0.56),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 80248.8,
    )
}

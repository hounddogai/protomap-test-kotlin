package com.acme.transport.customs.broker.review

import com.acme.transport.kit.numbers.roundTo

/** One step of the broker cycle calculation. */
interface BrokerCycleStep {
    fun describe(): String
    fun adjust(item: BrokerCycle, value: Double): Double
}

data class RoundingBrokerCycleAdjuster(var step: Double) : BrokerCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerCycle, value: Double): Double = roundTo(value, step)
}

data class BrokerCycleMarginOffsetAdjuster(var weight: Double) : BrokerCycleStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: BrokerCycle, value: Double): Double = value + item.margin * weight
}

data class TieredBrokerCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the broker cycle steps in order and keeps a trace of each. */
data class BrokerCycleEngine(
    var steps: MutableList<BrokerCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerCycle): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerCycleEngine(): BrokerCycleEngine {
    return BrokerCycleEngine(
        steps = mutableListOf<BrokerCycleStep>(
            RoundingBrokerCycleAdjuster(0.05),
            BrokerCycleMarginOffsetAdjuster(0.47),
            TieredBrokerCycleAdjuster(
                mutableListOf<Double>(5024.0, 6991.0, 7787.0),
                mutableListOf<Double>(1.18, 1.1, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 19250.4,
    )
}

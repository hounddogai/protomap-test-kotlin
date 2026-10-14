package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker plan calculation. */
interface BrokerPlanFactor {
    fun describe(): String
    fun adjust(item: BrokerPlan, value: Double): Double
}

data class TitleBrokerPlanAdjuster(var factors: MutableMap<String, Double>) : BrokerPlanFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BrokerPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BrokerPlanRetriesOffsetAdjuster(var weight: Double) : BrokerPlanFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: BrokerPlan, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentBrokerPlanAdjuster(var percent: Double) : BrokerPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBrokerPlanAdjuster(var cap: Double) : BrokerPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerPlan, value: Double): Double = minOf(value, cap)
}

data class TieredBrokerPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBrokerPlanAdjuster(var step: Double) : BrokerPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the broker plan steps in order and keeps a trace of each. */
data class BrokerPlanEngine(
    var steps: MutableList<BrokerPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BrokerPlan): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBrokerPlanEngine(): BrokerPlanEngine {
    return BrokerPlanEngine(
        steps = mutableListOf<BrokerPlanFactor>(
            TitleBrokerPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 1.36, "west" to 1.25, "central" to 1.12),
            ),
            BrokerPlanRetriesOffsetAdjuster(0.24),
            PercentBrokerPlanAdjuster(-24.1),
            CapBrokerPlanAdjuster(13446.0),
            TieredBrokerPlanAdjuster(mutableListOf<Double>(2211.0, 2904.0), mutableListOf<Double>(1.17, 0.99)),
            RoundingBrokerPlanAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 98018.1,
    )
}

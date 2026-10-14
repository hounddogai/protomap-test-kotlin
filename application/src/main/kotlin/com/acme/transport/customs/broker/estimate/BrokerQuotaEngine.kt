package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker quota calculation. */
interface BrokerQuotaAdjuster {
    fun describe(): String
    fun adjust(item: BrokerQuota, value: Double): Double
}

data class RoundingBrokerQuotaAdjuster(var step: Double) : BrokerQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerQuota, value: Double): Double = roundTo(value, step)
}

data class RegionBrokerQuotaAdjuster(var factors: MutableMap<String, Double>) : BrokerQuotaAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: BrokerQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBrokerQuotaAdjuster(var percent: Double) : BrokerQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker quota steps in order and keeps a trace of each. */
data class BrokerQuotaEngine(
    var steps: MutableList<BrokerQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerQuotaEngine(): BrokerQuotaEngine {
    return BrokerQuotaEngine(
        steps = mutableListOf<BrokerQuotaAdjuster>(
            RoundingBrokerQuotaAdjuster(0.05),
            RegionBrokerQuotaAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
            PercentBrokerQuotaAdjuster(-20.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 92848.4,
    )
}

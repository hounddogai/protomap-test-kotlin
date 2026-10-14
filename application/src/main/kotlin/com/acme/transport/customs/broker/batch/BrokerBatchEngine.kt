package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker batch calculation. */
interface BrokerBatchFactor {
    fun describe(): String
    fun adjust(item: BrokerBatch, value: Double): Double
}

data class RoundingBrokerBatchAdjuster(var step: Double) : BrokerBatchFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerBatch, value: Double): Double = roundTo(value, step)
}

data class TieredBrokerBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentBrokerBatchAdjuster(var factors: MutableMap<String, Double>) : BrokerBatchFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: BrokerBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBrokerBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBrokerBatchAdjuster(var cap: Double) : BrokerBatchFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerBatch, value: Double): Double = minOf(value, cap)
}

data class PercentBrokerBatchAdjuster(var percent: Double) : BrokerBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker batch steps in order and keeps a trace of each. */
data class BrokerBatchEngine(
    var steps: MutableList<BrokerBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerBatch): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerBatchEngine(flags: Registry): BrokerBatchEngine {
    return BrokerBatchEngine(
        steps = mutableListOf<BrokerBatchFactor>(
            RoundingBrokerBatchAdjuster(0.05),
            TieredBrokerBatchAdjuster(
                mutableListOf<Double>(587.0, 4024.0, 5876.0),
                mutableListOf<Double>(0.87, 1.01, 1.19),
            ),
            SegmentBrokerBatchAdjuster(mutableMapOf<String, Double>("south" to 1.22)),
            FlaggedBrokerBatchAdjuster(flags, "broker-batch-tiered", 0.8),
            CapBrokerBatchAdjuster(28658.0),
            PercentBrokerBatchAdjuster(-19.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 33607.9,
    )
}

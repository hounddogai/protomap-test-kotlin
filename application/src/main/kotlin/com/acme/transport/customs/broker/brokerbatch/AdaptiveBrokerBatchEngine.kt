package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive broker batch calculation. */
interface AdaptiveBrokerBatchStep {
    fun describe(): String
    fun adjust(item: AdaptiveBrokerBatch, value: Double): Double
}

data class FlaggedAdaptiveBrokerBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveBrokerBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveBrokerBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceAdaptiveBrokerBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveBrokerBatchStep {
    override fun describe(): String = "by source"

    override fun adjust(item: AdaptiveBrokerBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveBrokerBatchAdjuster(var minimum: Double) : AdaptiveBrokerBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveBrokerBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveBrokerBatchAdjuster(var step: Double) : AdaptiveBrokerBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveBrokerBatch, value: Double): Double = roundTo(value, step)
}

data class AdaptiveBrokerBatchCreatedAtOffsetAdjuster(var weight: Double) : AdaptiveBrokerBatchStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: AdaptiveBrokerBatch, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the adaptive broker batch steps in order and keeps a trace of each. */
data class AdaptiveBrokerBatchEngine(
    var steps: MutableList<AdaptiveBrokerBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveBrokerBatch): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveBrokerBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveBrokerBatchEngine(flags: Registry): AdaptiveBrokerBatchEngine {
    return AdaptiveBrokerBatchEngine(
        steps = mutableListOf<AdaptiveBrokerBatchStep>(
            FlaggedAdaptiveBrokerBatchAdjuster(flags, "adaptive-broker-batch-default", 9.0),
            SourceAdaptiveBrokerBatchAdjuster(
                mutableMapOf<String, Double>("north" to 0.97, "retail" to 1.16, "online" to 1.32),
            ),
            MinimumAdaptiveBrokerBatchAdjuster(14.7),
            RoundingAdaptiveBrokerBatchAdjuster(1.0),
            AdaptiveBrokerBatchCreatedAtOffsetAdjuster(1.16),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 80707.9,
    )
}

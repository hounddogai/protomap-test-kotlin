package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched broker rule calculation. */
interface BatchedBrokerRuleFactor {
    fun describe(): String
    fun adjust(item: BatchedBrokerRule, value: Double): Double
}

data class MinimumBatchedBrokerRuleAdjuster(var minimum: Double) : BatchedBrokerRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedBrokerRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedBrokerRuleAdjuster(var percent: Double) : BatchedBrokerRuleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedBrokerRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedBrokerRuleAdjuster(var step: Double) : BatchedBrokerRuleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedBrokerRule, value: Double): Double = roundTo(value, step)
}

data class CapBatchedBrokerRuleAdjuster(var cap: Double) : BatchedBrokerRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedBrokerRule, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedBrokerRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedBrokerRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedBrokerRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched broker rule steps in order and keeps a trace of each. */
data class BatchedBrokerRuleEngine(
    var steps: MutableList<BatchedBrokerRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedBrokerRule): Double {
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

    fun evaluateAll(items: MutableList<BatchedBrokerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedBrokerRuleEngine(flags: Registry): BatchedBrokerRuleEngine {
    return BatchedBrokerRuleEngine(
        steps = mutableListOf<BatchedBrokerRuleFactor>(
            MinimumBatchedBrokerRuleAdjuster(20.4),
            PercentBatchedBrokerRuleAdjuster(-1.5),
            RoundingBatchedBrokerRuleAdjuster(0.1),
            CapBatchedBrokerRuleAdjuster(1566.0),
            FlaggedBatchedBrokerRuleAdjuster(flags, "batched-broker-rule-tiered", -5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 16794.4,
    )
}

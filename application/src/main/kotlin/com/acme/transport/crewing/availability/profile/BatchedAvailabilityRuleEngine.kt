package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched availability rule calculation. */
interface BatchedAvailabilityRuleStep {
    fun describe(): String
    fun adjust(item: BatchedAvailabilityRule, value: Double): Double
}

data class PercentBatchedAvailabilityRuleAdjuster(var percent: Double) : BatchedAvailabilityRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAvailabilityRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedAvailabilityRuleAdjuster(var minimum: Double) : BatchedAvailabilityRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAvailabilityRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedAvailabilityRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAvailabilityRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAvailabilityRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedAvailabilityRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAvailabilityRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAvailabilityRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedAvailabilityRuleAdjuster(var cap: Double) : BatchedAvailabilityRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedAvailabilityRule, value: Double): Double = minOf(value, cap)
}

/** Runs the batched availability rule steps in order and keeps a trace of each. */
data class BatchedAvailabilityRuleEngine(
    var steps: MutableList<BatchedAvailabilityRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedAvailabilityRule): Double {
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

    fun evaluateAll(items: MutableList<BatchedAvailabilityRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedAvailabilityRuleEngine(flags: Registry): BatchedAvailabilityRuleEngine {
    return BatchedAvailabilityRuleEngine(
        steps = mutableListOf<BatchedAvailabilityRuleStep>(
            PercentBatchedAvailabilityRuleAdjuster(-19.2),
            MinimumBatchedAvailabilityRuleAdjuster(46.8),
            FlaggedBatchedAvailabilityRuleAdjuster(flags, "batched-availability-rule-scheduled", 1.1),
            TieredBatchedAvailabilityRuleAdjuster(
                mutableListOf<Double>(1797.0, 7708.0, 8452.0),
                mutableListOf<Double>(0.94, 0.91, 1.05),
            ),
            CapBatchedAvailabilityRuleAdjuster(32728.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 33616.9,
    )
}

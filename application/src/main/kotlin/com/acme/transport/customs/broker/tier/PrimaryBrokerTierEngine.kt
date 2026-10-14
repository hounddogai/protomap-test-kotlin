package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.flags.Registry

/** One step of the primary broker tier calculation. */
interface PrimaryBrokerTierStep {
    fun describe(): String
    fun adjust(item: PrimaryBrokerTier, value: Double): Double
}

data class FlaggedPrimaryBrokerTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryBrokerTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryBrokerTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryBrokerTierAdjuster(var cap: Double) : PrimaryBrokerTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryBrokerTier, value: Double): Double = minOf(value, cap)
}

data class LabelPrimaryBrokerTierAdjuster(var factors: MutableMap<String, Double>) : PrimaryBrokerTierStep {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryBrokerTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryBrokerTierCreatedAtOffsetAdjuster(var weight: Double) : PrimaryBrokerTierStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: PrimaryBrokerTier, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class TieredPrimaryBrokerTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryBrokerTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryBrokerTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary broker tier steps in order and keeps a trace of each. */
data class PrimaryBrokerTierEngine(
    var steps: MutableList<PrimaryBrokerTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryBrokerTier): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryBrokerTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryBrokerTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryBrokerTierEngine(flags: Registry): PrimaryBrokerTierEngine {
    return PrimaryBrokerTierEngine(
        steps = mutableListOf<PrimaryBrokerTierStep>(
            FlaggedPrimaryBrokerTierAdjuster(flags, "primary-broker-tier-adaptive", 3.7),
            CapPrimaryBrokerTierAdjuster(30474.0),
            LabelPrimaryBrokerTierAdjuster(
                mutableMapOf<String, Double>("west" to 1.11, "east" to 0.87, "retail" to 1.12),
            ),
            PrimaryBrokerTierCreatedAtOffsetAdjuster(1.02),
            TieredPrimaryBrokerTierAdjuster(
                mutableListOf<Double>(384.0, 2109.0, 4350.0, 8958.0),
                mutableListOf<Double>(0.81, 0.94, 1.2, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 35290.8,
    )
}

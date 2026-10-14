package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict relay tier calculation. */
interface StrictRelayTierAdjuster {
    fun describe(): String
    fun adjust(item: StrictRelayTier, value: Double): Double
}

data class PercentStrictRelayTierAdjuster(var percent: Double) : StrictRelayTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRelayTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictRelayTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRelayTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRelayTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictRelayTierAdjuster(var step: Double) : StrictRelayTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRelayTier, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictRelayTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRelayTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRelayTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict relay tier steps in order and keeps a trace of each. */
data class StrictRelayTierEngine(
    var steps: MutableList<StrictRelayTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRelayTier): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRelayTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRelayTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRelayTierEngine(flags: Registry): StrictRelayTierEngine {
    return StrictRelayTierEngine(
        steps = mutableListOf<StrictRelayTierAdjuster>(
            PercentStrictRelayTierAdjuster(5.1),
            TieredStrictRelayTierAdjuster(
                mutableListOf<Double>(6134.0, 6571.0),
                mutableListOf<Double>(0.86, 0.98),
            ),
            RoundingStrictRelayTierAdjuster(1.0),
            FlaggedStrictRelayTierAdjuster(flags, "strict-relay-tier-batched", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 71188.9,
    )
}

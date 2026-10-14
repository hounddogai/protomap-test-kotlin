package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the relay digest calculation. */
interface RelayDigestStep {
    fun describe(): String
    fun adjust(item: RelayDigest, value: Double): Double
}

data class MinimumRelayDigestAdjuster(var minimum: Double) : RelayDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRelayDigestAdjuster(var step: Double) : RelayDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayDigest, value: Double): Double = roundTo(value, step)
}

data class PercentRelayDigestAdjuster(var percent: Double) : RelayDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RelayDigestVersionOffsetAdjuster(var weight: Double) : RelayDigestStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RelayDigest, value: Double): Double = value + item.version.toDouble() * weight
}

data class TieredRelayDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameRelayDigestAdjuster(var factors: MutableMap<String, Double>) : RelayDigestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RelayDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the relay digest steps in order and keeps a trace of each. */
data class RelayDigestEngine(
    var steps: MutableList<RelayDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayDigest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<RelayDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayDigestEngine(): RelayDigestEngine {
    return RelayDigestEngine(
        steps = mutableListOf<RelayDigestStep>(
            MinimumRelayDigestAdjuster(29.0),
            RoundingRelayDigestAdjuster(0.25),
            PercentRelayDigestAdjuster(1.4),
            RelayDigestVersionOffsetAdjuster(0.88),
            TieredRelayDigestAdjuster(
                mutableListOf<Double>(4432.0, 5104.0, 8938.0),
                mutableListOf<Double>(1.17, 1.09, 1.18),
            ),
            NameRelayDigestAdjuster(mutableMapOf<String, Double>("west" to 0.78)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 64976.2,
    )
}

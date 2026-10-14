package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the broker digest calculation. */
interface BrokerDigestStep {
    fun describe(): String
    fun adjust(item: BrokerDigest, value: Double): Double
}

data class CapBrokerDigestAdjuster(var cap: Double) : BrokerDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerDigest, value: Double): Double = minOf(value, cap)
}

data class TieredBrokerDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBrokerDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the broker digest steps in order and keeps a trace of each. */
data class BrokerDigestEngine(
    var steps: MutableList<BrokerDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BrokerDigest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBrokerDigestEngine(flags: Registry): BrokerDigestEngine {
    return BrokerDigestEngine(
        steps = mutableListOf<BrokerDigestStep>(
            CapBrokerDigestAdjuster(39604.0),
            TieredBrokerDigestAdjuster(
                mutableListOf<Double>(2704.0, 5528.0, 6522.0),
                mutableListOf<Double>(1.18, 0.96, 0.83),
            ),
            FlaggedBrokerDigestAdjuster(flags, "broker-digest-fallback", -10.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 65239.2,
    )
}

package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.numbers.roundTo

/** One step of the default backhaul digest calculation. */
interface DefaultBackhaulDigestStep {
    fun describe(): String
    fun adjust(item: DefaultBackhaulDigest, value: Double): Double
}

data class NameDefaultBackhaulDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultBackhaulDigestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultBackhaulDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultBackhaulDigestAdjuster(var percent: Double) : DefaultBackhaulDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultBackhaulDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultBackhaulDigestAdjuster(var step: Double) : DefaultBackhaulDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultBackhaulDigest, value: Double): Double = roundTo(value, step)
}

data class TieredDefaultBackhaulDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultBackhaulDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultBackhaulDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultBackhaulDigestAdjuster(var cap: Double) : DefaultBackhaulDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultBackhaulDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the default backhaul digest steps in order and keeps a trace of each. */
data class DefaultBackhaulDigestEngine(
    var steps: MutableList<DefaultBackhaulDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultBackhaulDigest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultBackhaulDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultBackhaulDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultBackhaulDigestEngine(): DefaultBackhaulDigestEngine {
    return DefaultBackhaulDigestEngine(
        steps = mutableListOf<DefaultBackhaulDigestStep>(
            NameDefaultBackhaulDigestAdjuster(mutableMapOf<String, Double>("retail" to 1.07)),
            PercentDefaultBackhaulDigestAdjuster(-4.4),
            RoundingDefaultBackhaulDigestAdjuster(0.1),
            TieredDefaultBackhaulDigestAdjuster(
                mutableListOf<Double>(225.0, 683.0, 1029.0, 1710.0),
                mutableListOf<Double>(0.93, 1.17, 1.01, 1.13),
            ),
            CapDefaultBackhaulDigestAdjuster(10849.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 36544.6,
    )
}

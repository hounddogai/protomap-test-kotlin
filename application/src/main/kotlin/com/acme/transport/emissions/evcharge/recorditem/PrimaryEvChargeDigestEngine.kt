package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary ev charge digest calculation. */
interface PrimaryEvChargeDigestStep {
    fun describe(): String
    fun adjust(item: PrimaryEvChargeDigest, value: Double): Double
}

data class FlaggedPrimaryEvChargeDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryEvChargeDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryEvChargeDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryEvChargeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryEvChargeDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryEvChargeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryEvChargeDigestAdjuster(var step: Double) : PrimaryEvChargeDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEvChargeDigest, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryEvChargeDigestAdjuster(var minimum: Double) : PrimaryEvChargeDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryEvChargeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryEvChargeDigestAdjuster(var percent: Double) : PrimaryEvChargeDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryEvChargeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary ev charge digest steps in order and keeps a trace of each. */
data class PrimaryEvChargeDigestEngine(
    var steps: MutableList<PrimaryEvChargeDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryEvChargeDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryEvChargeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryEvChargeDigestEngine(flags: Registry): PrimaryEvChargeDigestEngine {
    return PrimaryEvChargeDigestEngine(
        steps = mutableListOf<PrimaryEvChargeDigestStep>(
            FlaggedPrimaryEvChargeDigestAdjuster(flags, "primary-ev-charge-digest-lenient", 0.4),
            TieredPrimaryEvChargeDigestAdjuster(
                mutableListOf<Double>(5993.0, 9112.0),
                mutableListOf<Double>(0.8, 1.13),
            ),
            RoundingPrimaryEvChargeDigestAdjuster(5.0),
            MinimumPrimaryEvChargeDigestAdjuster(36.4),
            PercentPrimaryEvChargeDigestAdjuster(14.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 74448.8,
    )
}

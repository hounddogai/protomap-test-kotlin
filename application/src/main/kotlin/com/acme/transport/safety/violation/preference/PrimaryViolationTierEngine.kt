package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary violation tier calculation. */
interface PrimaryViolationTierAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryViolationTier, value: Double): Double
}

data class FlaggedPrimaryViolationTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryViolationTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryViolationTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryViolationTierAdjuster(var cap: Double) : PrimaryViolationTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryViolationTier, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryViolationTierAdjuster(var percent: Double) : PrimaryViolationTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryViolationTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryViolationTierAdjuster(var step: Double) : PrimaryViolationTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryViolationTier, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryViolationTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryViolationTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryViolationTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary violation tier steps in order and keeps a trace of each. */
data class PrimaryViolationTierEngine(
    var steps: MutableList<PrimaryViolationTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryViolationTier): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryViolationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryViolationTierEngine(flags: Registry): PrimaryViolationTierEngine {
    return PrimaryViolationTierEngine(
        steps = mutableListOf<PrimaryViolationTierAdjuster>(
            FlaggedPrimaryViolationTierAdjuster(flags, "primary-violation-tier-seasonal", -7.2),
            CapPrimaryViolationTierAdjuster(40764.0),
            PercentPrimaryViolationTierAdjuster(24.4),
            RoundingPrimaryViolationTierAdjuster(0.5),
            TieredPrimaryViolationTierAdjuster(
                mutableListOf<Double>(4736.0, 4808.0, 7007.0),
                mutableListOf<Double>(0.89, 0.85, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 2430.0,
    )
}

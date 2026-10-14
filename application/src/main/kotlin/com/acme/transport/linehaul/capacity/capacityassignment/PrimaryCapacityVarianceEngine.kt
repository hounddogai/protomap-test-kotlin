package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary capacity variance calculation. */
interface PrimaryCapacityVarianceStep {
    fun describe(): String
    fun adjust(item: PrimaryCapacityVariance, value: Double): Double
}

data class TieredPrimaryCapacityVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCapacityVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCapacityVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryCapacityVarianceExpiresAtOffsetAdjuster(var weight: Double) : PrimaryCapacityVarianceStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: PrimaryCapacityVariance, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class FlaggedPrimaryCapacityVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCapacityVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCapacityVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryCapacityVarianceAdjuster(var step: Double) : PrimaryCapacityVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCapacityVariance, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryCapacityVarianceAdjuster(var cap: Double) : PrimaryCapacityVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCapacityVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the primary capacity variance steps in order and keeps a trace of each. */
data class PrimaryCapacityVarianceEngine(
    var steps: MutableList<PrimaryCapacityVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryCapacityVariance): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCapacityVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCapacityVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryCapacityVarianceEngine(flags: Registry): PrimaryCapacityVarianceEngine {
    return PrimaryCapacityVarianceEngine(
        steps = mutableListOf<PrimaryCapacityVarianceStep>(
            TieredPrimaryCapacityVarianceAdjuster(
                mutableListOf<Double>(4217.0, 6515.0, 9955.0),
                mutableListOf<Double>(1.17, 1.15, 1.09),
            ),
            PrimaryCapacityVarianceExpiresAtOffsetAdjuster(1.8),
            FlaggedPrimaryCapacityVarianceAdjuster(flags, "primary-capacity-variance-scheduled", -9.0),
            RoundingPrimaryCapacityVarianceAdjuster(0.5),
            CapPrimaryCapacityVarianceAdjuster(19350.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 65592.1,
    )
}

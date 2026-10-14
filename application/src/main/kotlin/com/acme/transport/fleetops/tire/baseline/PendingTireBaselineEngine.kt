package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending tire baseline calculation. */
interface PendingTireBaselineAdjuster {
    fun describe(): String
    fun adjust(item: PendingTireBaseline, value: Double): Double
}

data class FlaggedPendingTireBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTireBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTireBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingTireBaselineAdjuster(var step: Double) : PendingTireBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTireBaseline, value: Double): Double = roundTo(value, step)
}

data class PercentPendingTireBaselineAdjuster(var percent: Double) : PendingTireBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingTireBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingTireBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTireBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTireBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingTireBaselineAdjuster(var cap: Double) : PendingTireBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTireBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the pending tire baseline steps in order and keeps a trace of each. */
data class PendingTireBaselineEngine(
    var steps: MutableList<PendingTireBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingTireBaseline): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTireBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTireBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingTireBaselineEngine(flags: Registry): PendingTireBaselineEngine {
    return PendingTireBaselineEngine(
        steps = mutableListOf<PendingTireBaselineAdjuster>(
            FlaggedPendingTireBaselineAdjuster(flags, "pending-tire-baseline-manual", -2.0),
            RoundingPendingTireBaselineAdjuster(0.05),
            PercentPendingTireBaselineAdjuster(10.6),
            TieredPendingTireBaselineAdjuster(
                mutableListOf<Double>(4884.0, 5737.0, 8028.0),
                mutableListOf<Double>(1.14, 1.2, 1.17),
            ),
            CapPendingTireBaselineAdjuster(21851.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 60332.0,
    )
}

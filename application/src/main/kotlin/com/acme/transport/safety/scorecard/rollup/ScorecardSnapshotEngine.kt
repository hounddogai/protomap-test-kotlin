package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard snapshot calculation. */
interface ScorecardSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardSnapshot, value: Double): Double
}

data class LabelScorecardSnapshotAdjuster(var factors: MutableMap<String, Double>) : ScorecardSnapshotAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScorecardSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScorecardSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScorecardSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScorecardSnapshotAdjuster(var step: Double) : ScorecardSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardSnapshot, value: Double): Double = roundTo(value, step)
}

data class CapScorecardSnapshotAdjuster(var cap: Double) : ScorecardSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the scorecard snapshot steps in order and keeps a trace of each. */
data class ScorecardSnapshotEngine(
    var steps: MutableList<ScorecardSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardSnapshotEngine(flags: Registry): ScorecardSnapshotEngine {
    return ScorecardSnapshotEngine(
        steps = mutableListOf<ScorecardSnapshotAdjuster>(
            LabelScorecardSnapshotAdjuster(
                mutableMapOf<String, Double>("south" to 0.92, "retail" to 1.0, "west" to 1.04),
            ),
            TieredScorecardSnapshotAdjuster(
                mutableListOf<Double>(3786.0, 5048.0, 6950.0),
                mutableListOf<Double>(0.96, 1.14, 0.99),
            ),
            FlaggedScorecardSnapshotAdjuster(flags, "scorecard-snapshot-global", -0.7),
            RoundingScorecardSnapshotAdjuster(0.05),
            CapScorecardSnapshotAdjuster(31573.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 46372.5,
    )
}

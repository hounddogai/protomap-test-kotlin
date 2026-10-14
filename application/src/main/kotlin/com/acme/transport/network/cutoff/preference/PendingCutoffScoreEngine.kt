package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending cutoff score calculation. */
interface PendingCutoffScoreModifier {
    fun describe(): String
    fun adjust(item: PendingCutoffScore, value: Double): Double
}

data class PendingCutoffScoreDistanceKmOffsetAdjuster(var weight: Double) : PendingCutoffScoreModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: PendingCutoffScore, value: Double): Double = value + item.distanceKm * weight
}

data class TieredPendingCutoffScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCutoffScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCutoffScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingCutoffScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCutoffScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCutoffScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingCutoffScoreAdjuster(var percent: Double) : PendingCutoffScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCutoffScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingCutoffScoreAdjuster(var step: Double) : PendingCutoffScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCutoffScore, value: Double): Double = roundTo(value, step)
}

/** Runs the pending cutoff score steps in order and keeps a trace of each. */
data class PendingCutoffScoreEngine(
    var steps: MutableList<PendingCutoffScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingCutoffScore): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCutoffScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCutoffScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingCutoffScoreEngine(flags: Registry): PendingCutoffScoreEngine {
    return PendingCutoffScoreEngine(
        steps = mutableListOf<PendingCutoffScoreModifier>(
            PendingCutoffScoreDistanceKmOffsetAdjuster(0.69),
            TieredPendingCutoffScoreAdjuster(
                mutableListOf<Double>(6993.0, 7184.0, 9118.0, 9781.0),
                mutableListOf<Double>(0.99, 0.94, 1.14, 1.18),
            ),
            FlaggedPendingCutoffScoreAdjuster(flags, "pending-cutoff-score-strict", -6.0),
            PercentPendingCutoffScoreAdjuster(-14.1),
            RoundingPendingCutoffScoreAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 62402.0,
    )
}

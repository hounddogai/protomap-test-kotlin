package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scorecard digest calculation. */
interface ScorecardDigestModifier {
    fun describe(): String
    fun adjust(item: ScorecardDigest, value: Double): Double
}

data class CapScorecardDigestAdjuster(var cap: Double) : ScorecardDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardDigest, value: Double): Double = minOf(value, cap)
}

data class PercentScorecardDigestAdjuster(var percent: Double) : ScorecardDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScorecardDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScorecardDigestAdjuster(var minimum: Double) : ScorecardDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScorecardDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScorecardDigestAdjuster(var step: Double) : ScorecardDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the scorecard digest steps in order and keeps a trace of each. */
data class ScorecardDigestEngine(
    var steps: MutableList<ScorecardDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScorecardDigest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScorecardDigestEngine(flags: Registry): ScorecardDigestEngine {
    return ScorecardDigestEngine(
        steps = mutableListOf<ScorecardDigestModifier>(
            CapScorecardDigestAdjuster(40642.0),
            PercentScorecardDigestAdjuster(-1.8),
            FlaggedScorecardDigestAdjuster(flags, "scorecard-digest-legacy", -3.5),
            MinimumScorecardDigestAdjuster(20.8),
            TieredScorecardDigestAdjuster(
                mutableListOf<Double>(5627.0, 6490.0, 7822.0),
                mutableListOf<Double>(0.92, 1.01, 0.84),
            ),
            RoundingScorecardDigestAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 24872.6,
    )
}

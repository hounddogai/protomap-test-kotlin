package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the roster score calculation. */
interface RosterScoreModifier {
    fun describe(): String
    fun adjust(item: RosterScore, value: Double): Double
}

data class RoundingRosterScoreAdjuster(var step: Double) : RosterScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterScore, value: Double): Double = roundTo(value, step)
}

data class CapRosterScoreAdjuster(var cap: Double) : RosterScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterScore, value: Double): Double = minOf(value, cap)
}

data class PercentRosterScoreAdjuster(var percent: Double) : RosterScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRosterScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelRosterScoreAdjuster(var factors: MutableMap<String, Double>) : RosterScoreModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RosterScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRosterScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster score steps in order and keeps a trace of each. */
data class RosterScoreEngine(
    var steps: MutableList<RosterScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterScore): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterScoreEngine(flags: Registry): RosterScoreEngine {
    return RosterScoreEngine(
        steps = mutableListOf<RosterScoreModifier>(
            RoundingRosterScoreAdjuster(0.05),
            CapRosterScoreAdjuster(579.0),
            PercentRosterScoreAdjuster(9.2),
            FlaggedRosterScoreAdjuster(flags, "roster-score-fallback", -5.1),
            LabelRosterScoreAdjuster(mutableMapOf<String, Double>("online" to 1.09, "south" to 1.1)),
            TieredRosterScoreAdjuster(mutableListOf<Double>(2655.0, 8786.0), mutableListOf<Double>(0.85, 1.15)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 65440.3,
    )
}

package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the roster tier calculation. */
interface RosterTierStep {
    fun describe(): String
    fun adjust(item: RosterTier, value: Double): Double
}

data class CapRosterTierAdjuster(var cap: Double) : RosterTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterTier, value: Double): Double = minOf(value, cap)
}

data class PercentRosterTierAdjuster(var percent: Double) : RosterTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRosterTierAdjuster(var step: Double) : RosterTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterTier, value: Double): Double = roundTo(value, step)
}

data class TitleRosterTierAdjuster(var factors: MutableMap<String, Double>) : RosterTierStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RosterTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRosterTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster tier steps in order and keeps a trace of each. */
data class RosterTierEngine(
    var steps: MutableList<RosterTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RosterTier): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRosterTierEngine(): RosterTierEngine {
    return RosterTierEngine(
        steps = mutableListOf<RosterTierStep>(
            CapRosterTierAdjuster(42520.0),
            PercentRosterTierAdjuster(-17.9),
            RoundingRosterTierAdjuster(0.25),
            TitleRosterTierAdjuster(
                mutableMapOf<String, Double>("online" to 1.19, "central" to 0.74, "west" to 0.71),
            ),
            TieredRosterTierAdjuster(
                mutableListOf<Double>(2316.0, 5753.0, 7661.0, 8817.0),
                mutableListOf<Double>(1.12, 1.17, 0.9, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 82962.6,
    )
}

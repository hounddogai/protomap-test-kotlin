package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the roster baseline calculation. */
interface RosterBaselineModifier {
    fun describe(): String
    fun adjust(item: RosterBaseline, value: Double): Double
}

data class CapRosterBaselineAdjuster(var cap: Double) : RosterBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterBaseline, value: Double): Double = minOf(value, cap)
}

data class LabelRosterBaselineAdjuster(var factors: MutableMap<String, Double>) : RosterBaselineModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RosterBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRosterBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRosterBaselineAdjuster(var percent: Double) : RosterBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRosterBaselineAdjuster(var step: Double) : RosterBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the roster baseline steps in order and keeps a trace of each. */
data class RosterBaselineEngine(
    var steps: MutableList<RosterBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterBaseline): Double {
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

    fun evaluateAll(items: MutableList<RosterBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterBaselineEngine(flags: Registry): RosterBaselineEngine {
    return RosterBaselineEngine(
        steps = mutableListOf<RosterBaselineModifier>(
            CapRosterBaselineAdjuster(39537.0),
            LabelRosterBaselineAdjuster(
                mutableMapOf<String, Double>("west" to 1.05, "online" to 1.04, "retail" to 0.75),
            ),
            FlaggedRosterBaselineAdjuster(flags, "roster-baseline-regional", -7.2),
            PercentRosterBaselineAdjuster(7.6),
            RoundingRosterBaselineAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 18978.8,
    )
}

package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the roster draft calculation. */
interface RosterDraftModifier {
    fun describe(): String
    fun adjust(item: RosterDraft, value: Double): Double
}

data class CapRosterDraftAdjuster(var cap: Double) : RosterDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterDraft, value: Double): Double = minOf(value, cap)
}

data class FlaggedRosterDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelRosterDraftAdjuster(var factors: MutableMap<String, Double>) : RosterDraftModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RosterDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RosterDraftSequenceOffsetAdjuster(var weight: Double) : RosterDraftModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: RosterDraft, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class RoundingRosterDraftAdjuster(var step: Double) : RosterDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterDraft, value: Double): Double = roundTo(value, step)
}

data class TieredRosterDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster draft steps in order and keeps a trace of each. */
data class RosterDraftEngine(
    var steps: MutableList<RosterDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterDraft): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RosterDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterDraftEngine(flags: Registry): RosterDraftEngine {
    return RosterDraftEngine(
        steps = mutableListOf<RosterDraftModifier>(
            CapRosterDraftAdjuster(28543.0),
            FlaggedRosterDraftAdjuster(flags, "roster-draft-regional", 4.9),
            LabelRosterDraftAdjuster(mutableMapOf<String, Double>("north" to 1.27, "online" to 1.02)),
            RosterDraftSequenceOffsetAdjuster(0.41),
            RoundingRosterDraftAdjuster(0.1),
            TieredRosterDraftAdjuster(mutableListOf<Double>(1178.0, 2192.0), mutableListOf<Double>(1.09, 0.86)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 36161.0,
    )
}

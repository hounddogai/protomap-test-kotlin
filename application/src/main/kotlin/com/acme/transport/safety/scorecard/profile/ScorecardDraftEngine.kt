package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scorecard draft calculation. */
interface ScorecardDraftAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardDraft, value: Double): Double
}

data class FlaggedScorecardDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScorecardDraftAdjuster(var cap: Double) : ScorecardDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumScorecardDraftAdjuster(var minimum: Double) : ScorecardDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scorecard draft steps in order and keeps a trace of each. */
data class ScorecardDraftEngine(
    var steps: MutableList<ScorecardDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardDraft): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardDraftEngine(flags: Registry): ScorecardDraftEngine {
    return ScorecardDraftEngine(
        steps = mutableListOf<ScorecardDraftAdjuster>(
            FlaggedScorecardDraftAdjuster(flags, "scorecard-draft-manual", -0.2),
            CapScorecardDraftAdjuster(32537.0),
            MinimumScorecardDraftAdjuster(18.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 17035.8,
    )
}

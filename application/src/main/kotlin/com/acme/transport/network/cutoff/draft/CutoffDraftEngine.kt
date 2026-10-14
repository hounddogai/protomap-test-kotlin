package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff draft calculation. */
interface CutoffDraftAdjuster {
    fun describe(): String
    fun adjust(item: CutoffDraft, value: Double): Double
}

data class PercentCutoffDraftAdjuster(var percent: Double) : CutoffDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCutoffDraftAdjuster(var step: Double) : CutoffDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffDraft, value: Double): Double = roundTo(value, step)
}

data class CapCutoffDraftAdjuster(var cap: Double) : CutoffDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffDraft, value: Double): Double = minOf(value, cap)
}

data class FlaggedCutoffDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the cutoff draft steps in order and keeps a trace of each. */
data class CutoffDraftEngine(
    var steps: MutableList<CutoffDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffDraftEngine(flags: Registry): CutoffDraftEngine {
    return CutoffDraftEngine(
        steps = mutableListOf<CutoffDraftAdjuster>(
            PercentCutoffDraftAdjuster(-18.4),
            RoundingCutoffDraftAdjuster(0.05),
            CapCutoffDraftAdjuster(39678.0),
            FlaggedCutoffDraftAdjuster(flags, "cutoff-draft-weighted", 7.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 68686.9,
    )
}

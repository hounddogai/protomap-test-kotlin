package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.flags.Registry

/** One step of the cutoff segment calculation. */
interface CutoffSegmentModifier {
    fun describe(): String
    fun adjust(item: CutoffSegment, value: Double): Double
}

data class CategoryCutoffSegmentAdjuster(var factors: MutableMap<String, Double>) : CutoffSegmentModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: CutoffSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCutoffSegmentAdjuster(var cap: Double) : CutoffSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffSegment, value: Double): Double = minOf(value, cap)
}

data class CutoffSegmentEndsAtOffsetAdjuster(var weight: Double) : CutoffSegmentModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CutoffSegment, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class FlaggedCutoffSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCutoffSegmentAdjuster(var percent: Double) : CutoffSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cutoff segment steps in order and keeps a trace of each. */
data class CutoffSegmentEngine(
    var steps: MutableList<CutoffSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffSegment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffSegmentEngine(flags: Registry): CutoffSegmentEngine {
    return CutoffSegmentEngine(
        steps = mutableListOf<CutoffSegmentModifier>(
            CategoryCutoffSegmentAdjuster(mutableMapOf<String, Double>("north" to 0.9, "retail" to 1.36)),
            CapCutoffSegmentAdjuster(33678.0),
            CutoffSegmentEndsAtOffsetAdjuster(1.95),
            FlaggedCutoffSegmentAdjuster(flags, "cutoff-segment-strict", 9.0),
            PercentCutoffSegmentAdjuster(-8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 13840.8,
    )
}

package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.flags.Registry

/** One step of the lenient cutoff segment calculation. */
interface LenientCutoffSegmentModifier {
    fun describe(): String
    fun adjust(item: LenientCutoffSegment, value: Double): Double
}

data class CountryLenientCutoffSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCutoffSegmentModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientCutoffSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientCutoffSegmentAdjuster(var minimum: Double) : LenientCutoffSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCutoffSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientCutoffSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCutoffSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCutoffSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientCutoffSegmentQuantityOffsetAdjuster(var weight: Double) : LenientCutoffSegmentModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LenientCutoffSegment, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the lenient cutoff segment steps in order and keeps a trace of each. */
data class LenientCutoffSegmentEngine(
    var steps: MutableList<LenientCutoffSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientCutoffSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCutoffSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCutoffSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientCutoffSegmentEngine(flags: Registry): LenientCutoffSegmentEngine {
    return LenientCutoffSegmentEngine(
        steps = mutableListOf<LenientCutoffSegmentModifier>(
            CountryLenientCutoffSegmentAdjuster(mutableMapOf<String, Double>("west" to 1.34, "retail" to 1.36)),
            MinimumLenientCutoffSegmentAdjuster(48.3),
            FlaggedLenientCutoffSegmentAdjuster(flags, "lenient-cutoff-segment-manual", 8.6),
            LenientCutoffSegmentQuantityOffsetAdjuster(1.22),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 2074.8,
    )
}

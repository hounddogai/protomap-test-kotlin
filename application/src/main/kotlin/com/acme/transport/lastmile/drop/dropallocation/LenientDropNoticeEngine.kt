package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient drop notice calculation. */
interface LenientDropNoticeModifier {
    fun describe(): String
    fun adjust(item: LenientDropNotice, value: Double): Double
}

data class CapLenientDropNoticeAdjuster(var cap: Double) : LenientDropNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDropNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientDropNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDropNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDropNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLenientDropNoticeAdjuster(var step: Double) : LenientDropNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDropNotice, value: Double): Double = roundTo(value, step)
}

data class LaneLenientDropNoticeAdjuster(var factors: MutableMap<String, Double>) : LenientDropNoticeModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: LenientDropNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientDropNoticeAdjuster(var percent: Double) : LenientDropNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDropNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient drop notice steps in order and keeps a trace of each. */
data class LenientDropNoticeEngine(
    var steps: MutableList<LenientDropNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDropNotice): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientDropNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientDropNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDropNoticeEngine(flags: Registry): LenientDropNoticeEngine {
    return LenientDropNoticeEngine(
        steps = mutableListOf<LenientDropNoticeModifier>(
            CapLenientDropNoticeAdjuster(13965.0),
            FlaggedLenientDropNoticeAdjuster(flags, "lenient-drop-notice-scheduled", 3.0),
            RoundingLenientDropNoticeAdjuster(5.0),
            LaneLenientDropNoticeAdjuster(mutableMapOf<String, Double>("central" to 1.05, "east" to 0.78)),
            PercentLenientDropNoticeAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 25785.2,
    )
}

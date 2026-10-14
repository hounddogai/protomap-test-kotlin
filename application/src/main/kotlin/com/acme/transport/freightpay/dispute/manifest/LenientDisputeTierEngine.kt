package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient dispute tier calculation. */
interface LenientDisputeTierModifier {
    fun describe(): String
    fun adjust(item: LenientDisputeTier, value: Double): Double
}

data class TitleLenientDisputeTierAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDisputeTierModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientDisputeTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientDisputeTierAdjuster(var step: Double) : LenientDisputeTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDisputeTier, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientDisputeTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDisputeTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDisputeTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientDisputeTierAdjuster(var percent: Double) : LenientDisputeTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDisputeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientDisputeTierAmountOffsetAdjuster(var weight: Double) : LenientDisputeTierModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: LenientDisputeTier, value: Double): Double = value + item.amount * weight
}

data class CapLenientDisputeTierAdjuster(var cap: Double) : LenientDisputeTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDisputeTier, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient dispute tier steps in order and keeps a trace of each. */
data class LenientDisputeTierEngine(
    var steps: MutableList<LenientDisputeTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientDisputeTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientDisputeTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientDisputeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientDisputeTierEngine(flags: Registry): LenientDisputeTierEngine {
    return LenientDisputeTierEngine(
        steps = mutableListOf<LenientDisputeTierModifier>(
            TitleLenientDisputeTierAdjuster(
                mutableMapOf<String, Double>("west" to 0.74, "retail" to 1.24, "south" to 1.15),
            ),
            RoundingLenientDisputeTierAdjuster(0.1),
            FlaggedLenientDisputeTierAdjuster(flags, "lenient-dispute-tier-tiered", -3.9),
            PercentLenientDisputeTierAdjuster(25.0),
            LenientDisputeTierAmountOffsetAdjuster(0.98),
            CapLenientDisputeTierAdjuster(47723.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 72657.6,
    )
}

package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict roster baseline calculation. */
interface StrictRosterBaselineFactor {
    fun describe(): String
    fun adjust(item: StrictRosterBaseline, value: Double): Double
}

data class FlaggedStrictRosterBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRosterBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRosterBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapStrictRosterBaselineAdjuster(var cap: Double) : StrictRosterBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRosterBaseline, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictRosterBaselineAdjuster(var step: Double) : StrictRosterBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRosterBaseline, value: Double): Double = roundTo(value, step)
}

data class StrictRosterBaselineExpiresAtOffsetAdjuster(var weight: Double) : StrictRosterBaselineFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: StrictRosterBaseline, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class LocaleStrictRosterBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRosterBaselineFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: StrictRosterBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictRosterBaselineAdjuster(var percent: Double) : StrictRosterBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRosterBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict roster baseline steps in order and keeps a trace of each. */
data class StrictRosterBaselineEngine(
    var steps: MutableList<StrictRosterBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictRosterBaseline): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRosterBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRosterBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictRosterBaselineEngine(flags: Registry): StrictRosterBaselineEngine {
    return StrictRosterBaselineEngine(
        steps = mutableListOf<StrictRosterBaselineFactor>(
            FlaggedStrictRosterBaselineAdjuster(flags, "strict-roster-baseline-global", 5.7),
            CapStrictRosterBaselineAdjuster(39485.0),
            RoundingStrictRosterBaselineAdjuster(0.1),
            StrictRosterBaselineExpiresAtOffsetAdjuster(0.86),
            LocaleStrictRosterBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 0.72, "west" to 0.75, "online" to 1.04),
            ),
            PercentStrictRosterBaselineAdjuster(-5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 47732.0,
    )
}

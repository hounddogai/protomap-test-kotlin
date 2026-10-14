package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.Registry

/** One step of the lenient scorecard slot calculation. */
interface LenientScorecardSlotFactor {
    fun describe(): String
    fun adjust(item: LenientScorecardSlot, value: Double): Double
}

data class FlaggedLenientScorecardSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientScorecardSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientScorecardSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientScorecardSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientScorecardSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientScorecardSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientScorecardSlotAdjuster(var percent: Double) : LenientScorecardSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientScorecardSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientScorecardSlotAdjuster(var cap: Double) : LenientScorecardSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientScorecardSlot, value: Double): Double = minOf(value, cap)
}

data class CountryLenientScorecardSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientScorecardSlotFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientScorecardSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient scorecard slot steps in order and keeps a trace of each. */
data class LenientScorecardSlotEngine(
    var steps: MutableList<LenientScorecardSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientScorecardSlot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientScorecardSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientScorecardSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientScorecardSlotEngine(flags: Registry): LenientScorecardSlotEngine {
    return LenientScorecardSlotEngine(
        steps = mutableListOf<LenientScorecardSlotFactor>(
            FlaggedLenientScorecardSlotAdjuster(flags, "lenient-scorecard-slot-seasonal", 4.8),
            TieredLenientScorecardSlotAdjuster(
                mutableListOf<Double>(3422.0, 9203.0, 9747.0),
                mutableListOf<Double>(1.18, 1.1, 0.88),
            ),
            PercentLenientScorecardSlotAdjuster(12.6),
            CapLenientScorecardSlotAdjuster(5245.0),
            CountryLenientScorecardSlotAdjuster(mutableMapOf<String, Double>("north" to 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 72902.9,
    )
}

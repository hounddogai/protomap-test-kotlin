package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy rest break agreement calculation. */
interface LegacyRestBreakAgreementFactor {
    fun describe(): String
    fun adjust(item: LegacyRestBreakAgreement, value: Double): Double
}

data class RoundingLegacyRestBreakAgreementAdjuster(var step: Double) : LegacyRestBreakAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRestBreakAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyRestBreakAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRestBreakAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRestBreakAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyRestBreakAgreementAdjuster(var cap: Double) : LegacyRestBreakAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyRestBreakAgreement, value: Double): Double = minOf(value, cap)
}

data class SegmentLegacyRestBreakAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyRestBreakAgreementFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: LegacyRestBreakAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyRestBreakAgreementFuelLitersOffsetAdjuster(
    var weight: Double,
) : LegacyRestBreakAgreementFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: LegacyRestBreakAgreement, value: Double): Double = value + item.fuelLiters * weight
}

data class MinimumLegacyRestBreakAgreementAdjuster(var minimum: Double) : LegacyRestBreakAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRestBreakAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy rest break agreement steps in order and keeps a trace of each. */
data class LegacyRestBreakAgreementEngine(
    var steps: MutableList<LegacyRestBreakAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyRestBreakAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRestBreakAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRestBreakAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyRestBreakAgreementEngine(flags: Registry): LegacyRestBreakAgreementEngine {
    return LegacyRestBreakAgreementEngine(
        steps = mutableListOf<LegacyRestBreakAgreementFactor>(
            RoundingLegacyRestBreakAgreementAdjuster(0.1),
            FlaggedLegacyRestBreakAgreementAdjuster(flags, "legacy-rest-break-agreement-fallback", -9.8),
            CapLegacyRestBreakAgreementAdjuster(13636.0),
            SegmentLegacyRestBreakAgreementAdjuster(
                mutableMapOf<String, Double>("retail" to 1.18, "south" to 1.22, "north" to 1.19),
            ),
            LegacyRestBreakAgreementFuelLitersOffsetAdjuster(0.09),
            MinimumLegacyRestBreakAgreementAdjuster(22.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 62676.1,
    )
}

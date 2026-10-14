package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary tender draft calculation. */
interface PrimaryTenderDraftModifier {
    fun describe(): String
    fun adjust(item: PrimaryTenderDraft, value: Double): Double
}

data class TieredPrimaryTenderDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTenderDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTenderDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryTenderDraftAdjuster(var minimum: Double) : PrimaryTenderDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTenderDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryTenderDraftAdjuster(var step: Double) : PrimaryTenderDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTenderDraft, value: Double): Double = roundTo(value, step)
}

data class PercentPrimaryTenderDraftAdjuster(var percent: Double) : PrimaryTenderDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTenderDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPrimaryTenderDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTenderDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTenderDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryTenderDraftAdjuster(var cap: Double) : PrimaryTenderDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTenderDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the primary tender draft steps in order and keeps a trace of each. */
data class PrimaryTenderDraftEngine(
    var steps: MutableList<PrimaryTenderDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryTenderDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTenderDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTenderDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryTenderDraftEngine(flags: Registry): PrimaryTenderDraftEngine {
    return PrimaryTenderDraftEngine(
        steps = mutableListOf<PrimaryTenderDraftModifier>(
            TieredPrimaryTenderDraftAdjuster(
                mutableListOf<Double>(168.0, 7687.0),
                mutableListOf<Double>(0.91, 0.86),
            ),
            MinimumPrimaryTenderDraftAdjuster(3.5),
            RoundingPrimaryTenderDraftAdjuster(0.05),
            PercentPrimaryTenderDraftAdjuster(-8.3),
            FlaggedPrimaryTenderDraftAdjuster(flags, "primary-tender-draft-regional", 6.9),
            CapPrimaryTenderDraftAdjuster(34485.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 55905.6,
    )
}

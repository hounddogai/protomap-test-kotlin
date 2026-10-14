package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary lane plan draft calculation. */
interface PrimaryLanePlanDraftModifier {
    fun describe(): String
    fun adjust(item: PrimaryLanePlanDraft, value: Double): Double
}

data class RoundingPrimaryLanePlanDraftAdjuster(var step: Double) : PrimaryLanePlanDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryLanePlanDraft, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryLanePlanDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryLanePlanDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryLanePlanDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryLanePlanDraftAdjuster(var cap: Double) : PrimaryLanePlanDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryLanePlanDraft, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryLanePlanDraftAdjuster(var percent: Double) : PrimaryLanePlanDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryLanePlanDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPrimaryLanePlanDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryLanePlanDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryLanePlanDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelPrimaryLanePlanDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryLanePlanDraftModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: PrimaryLanePlanDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary lane plan draft steps in order and keeps a trace of each. */
data class PrimaryLanePlanDraftEngine(
    var steps: MutableList<PrimaryLanePlanDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryLanePlanDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryLanePlanDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryLanePlanDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryLanePlanDraftEngine(flags: Registry): PrimaryLanePlanDraftEngine {
    return PrimaryLanePlanDraftEngine(
        steps = mutableListOf<PrimaryLanePlanDraftModifier>(
            RoundingPrimaryLanePlanDraftAdjuster(1.0),
            FlaggedPrimaryLanePlanDraftAdjuster(flags, "primary-lane-plan-draft-fallback", -5.2),
            CapPrimaryLanePlanDraftAdjuster(10367.0),
            PercentPrimaryLanePlanDraftAdjuster(8.6),
            TieredPrimaryLanePlanDraftAdjuster(
                mutableListOf<Double>(1087.0, 1540.0, 3592.0, 5834.0),
                mutableListOf<Double>(1.04, 1.09, 1.02, 1.03),
            ),
            ChannelPrimaryLanePlanDraftAdjuster(
                mutableMapOf<String, Double>("south" to 0.84, "north" to 0.87, "east" to 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 57398.8,
    )
}

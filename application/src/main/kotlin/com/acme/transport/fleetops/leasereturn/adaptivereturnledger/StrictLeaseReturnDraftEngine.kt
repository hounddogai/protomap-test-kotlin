package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict lease return draft calculation. */
interface StrictLeaseReturnDraftModifier {
    fun describe(): String
    fun adjust(item: StrictLeaseReturnDraft, value: Double): Double
}

data class StrictLeaseReturnDraftMinimumOffsetAdjuster(var weight: Double) : StrictLeaseReturnDraftModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: StrictLeaseReturnDraft, value: Double): Double = value + item.minimum * weight
}

data class LabelStrictLeaseReturnDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictLeaseReturnDraftModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictLeaseReturnDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictLeaseReturnDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictLeaseReturnDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictLeaseReturnDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictLeaseReturnDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictLeaseReturnDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictLeaseReturnDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictLeaseReturnDraftAdjuster(var step: Double) : StrictLeaseReturnDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictLeaseReturnDraft, value: Double): Double = roundTo(value, step)
}

data class CapStrictLeaseReturnDraftAdjuster(var cap: Double) : StrictLeaseReturnDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictLeaseReturnDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the strict lease return draft steps in order and keeps a trace of each. */
data class StrictLeaseReturnDraftEngine(
    var steps: MutableList<StrictLeaseReturnDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictLeaseReturnDraft): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictLeaseReturnDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictLeaseReturnDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictLeaseReturnDraftEngine(flags: Registry): StrictLeaseReturnDraftEngine {
    return StrictLeaseReturnDraftEngine(
        steps = mutableListOf<StrictLeaseReturnDraftModifier>(
            StrictLeaseReturnDraftMinimumOffsetAdjuster(0.34),
            LabelStrictLeaseReturnDraftAdjuster(mutableMapOf<String, Double>("online" to 1.34)),
            FlaggedStrictLeaseReturnDraftAdjuster(flags, "strict-lease-return-draft-pending", 4.8),
            TieredStrictLeaseReturnDraftAdjuster(
                mutableListOf<Double>(5530.0, 5535.0, 7541.0, 8201.0),
                mutableListOf<Double>(1.09, 0.96, 1.16, 1.16),
            ),
            RoundingStrictLeaseReturnDraftAdjuster(1.0),
            CapStrictLeaseReturnDraftAdjuster(38398.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 27550.2,
    )
}

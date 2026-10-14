package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped inspection draft calculation. */
interface CappedInspectionDraftModifier {
    fun describe(): String
    fun adjust(item: CappedInspectionDraft, value: Double): Double
}

data class TieredCappedInspectionDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedInspectionDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedInspectionDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedInspectionDraftAdjuster(var step: Double) : CappedInspectionDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedInspectionDraft, value: Double): Double = roundTo(value, step)
}

data class PercentCappedInspectionDraftAdjuster(var percent: Double) : CappedInspectionDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedInspectionDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedInspectionDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedInspectionDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedInspectionDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped inspection draft steps in order and keeps a trace of each. */
data class CappedInspectionDraftEngine(
    var steps: MutableList<CappedInspectionDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedInspectionDraft): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedInspectionDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedInspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedInspectionDraftEngine(flags: Registry): CappedInspectionDraftEngine {
    return CappedInspectionDraftEngine(
        steps = mutableListOf<CappedInspectionDraftModifier>(
            TieredCappedInspectionDraftAdjuster(
                mutableListOf<Double>(5530.0, 5697.0),
                mutableListOf<Double>(1.03, 0.85),
            ),
            RoundingCappedInspectionDraftAdjuster(0.05),
            PercentCappedInspectionDraftAdjuster(8.2),
            FlaggedCappedInspectionDraftAdjuster(flags, "capped-inspection-draft-seasonal", -9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 37391.3,
    )
}

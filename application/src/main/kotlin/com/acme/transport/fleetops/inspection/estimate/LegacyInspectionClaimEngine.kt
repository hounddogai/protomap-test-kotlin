package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the legacy inspection claim calculation. */
interface LegacyInspectionClaimModifier {
    fun describe(): String
    fun adjust(item: LegacyInspectionClaim, value: Double): Double
}

data class LegacyInspectionClaimRatioOffsetAdjuster(var weight: Double) : LegacyInspectionClaimModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: LegacyInspectionClaim, value: Double): Double = value + item.ratio * weight
}

data class TitleLegacyInspectionClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionClaimModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyInspectionClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyInspectionClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyInspectionClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyInspectionClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy inspection claim steps in order and keeps a trace of each. */
data class LegacyInspectionClaimEngine(
    var steps: MutableList<LegacyInspectionClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyInspectionClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyInspectionClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyInspectionClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyInspectionClaimEngine(flags: Registry): LegacyInspectionClaimEngine {
    return LegacyInspectionClaimEngine(
        steps = mutableListOf<LegacyInspectionClaimModifier>(
            LegacyInspectionClaimRatioOffsetAdjuster(1.72),
            TitleLegacyInspectionClaimAdjuster(
                mutableMapOf<String, Double>("retail" to 0.96, "south" to 0.88, "north" to 0.85),
            ),
            FlaggedLegacyInspectionClaimAdjuster(flags, "legacy-inspection-claim-scheduled", -5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 77742.2,
    )
}

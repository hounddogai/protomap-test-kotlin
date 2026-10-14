package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection draft calculation. */
interface InspectionDraftAdjuster {
    fun describe(): String
    fun adjust(item: InspectionDraft, value: Double): Double
}

data class CapInspectionDraftAdjuster(var cap: Double) : InspectionDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionDraft, value: Double): Double = minOf(value, cap)
}

data class PercentInspectionDraftAdjuster(var percent: Double) : InspectionDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedInspectionDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneInspectionDraftAdjuster(var factors: MutableMap<String, Double>) : InspectionDraftAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: InspectionDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection draft steps in order and keeps a trace of each. */
data class InspectionDraftEngine(
    var steps: MutableList<InspectionDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionDraft): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionDraftEngine(flags: Registry): InspectionDraftEngine {
    return InspectionDraftEngine(
        steps = mutableListOf<InspectionDraftAdjuster>(
            CapInspectionDraftAdjuster(38363.0),
            PercentInspectionDraftAdjuster(-21.0),
            FlaggedInspectionDraftAdjuster(flags, "inspection-draft-global", -2.3),
            LaneInspectionDraftAdjuster(mutableMapOf<String, Double>("north" to 1.25)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 64248.6,
    )
}

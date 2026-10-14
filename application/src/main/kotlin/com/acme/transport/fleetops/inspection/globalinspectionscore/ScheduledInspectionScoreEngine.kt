package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled inspection score calculation. */
interface ScheduledInspectionScoreModifier {
    fun describe(): String
    fun adjust(item: ScheduledInspectionScore, value: Double): Double
}

data class FlaggedScheduledInspectionScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledInspectionScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledInspectionScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledInspectionScoreCostOffsetAdjuster(var weight: Double) : ScheduledInspectionScoreModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ScheduledInspectionScore, value: Double): Double = value + item.cost * weight
}

data class LaneScheduledInspectionScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledInspectionScoreModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledInspectionScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled inspection score steps in order and keeps a trace of each. */
data class ScheduledInspectionScoreEngine(
    var steps: MutableList<ScheduledInspectionScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledInspectionScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledInspectionScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledInspectionScoreEngine(flags: Registry): ScheduledInspectionScoreEngine {
    return ScheduledInspectionScoreEngine(
        steps = mutableListOf<ScheduledInspectionScoreModifier>(
            FlaggedScheduledInspectionScoreAdjuster(flags, "scheduled-inspection-score-regional", -6.1),
            ScheduledInspectionScoreCostOffsetAdjuster(1.71),
            LaneScheduledInspectionScoreAdjuster(mutableMapOf<String, Double>("south" to 1.02)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 98643.4,
    )
}

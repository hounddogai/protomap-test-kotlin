package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection group calculation. */
interface InspectionGroupFactor {
    fun describe(): String
    fun adjust(item: InspectionGroup, value: Double): Double
}

data class InspectionGroupEtaMinutesOffsetAdjuster(var weight: Double) : InspectionGroupFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: InspectionGroup, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentInspectionGroupAdjuster(var percent: Double) : InspectionGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedInspectionGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection group steps in order and keeps a trace of each. */
data class InspectionGroupEngine(
    var steps: MutableList<InspectionGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionGroup): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionGroupEngine(flags: Registry): InspectionGroupEngine {
    return InspectionGroupEngine(
        steps = mutableListOf<InspectionGroupFactor>(
            InspectionGroupEtaMinutesOffsetAdjuster(0.42),
            PercentInspectionGroupAdjuster(-20.8),
            FlaggedInspectionGroupAdjuster(flags, "inspection-group-adaptive", 1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 24775.5,
    )
}

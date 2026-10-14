package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global inspection group calculation. */
interface GlobalInspectionGroupAdjuster {
    fun describe(): String
    fun adjust(item: GlobalInspectionGroup, value: Double): Double
}

data class GlobalInspectionGroupEndsAtOffsetAdjuster(var weight: Double) : GlobalInspectionGroupAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class NameGlobalInspectionGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalInspectionGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalInspectionGroupAdjuster(var step: Double) : GlobalInspectionGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalInspectionGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalInspectionGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalInspectionGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalInspectionGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalInspectionGroupAdjuster(var cap: Double) : GlobalInspectionGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the global inspection group steps in order and keeps a trace of each. */
data class GlobalInspectionGroupEngine(
    var steps: MutableList<GlobalInspectionGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalInspectionGroup): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalInspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalInspectionGroupEngine(flags: Registry): GlobalInspectionGroupEngine {
    return GlobalInspectionGroupEngine(
        steps = mutableListOf<GlobalInspectionGroupAdjuster>(
            GlobalInspectionGroupEndsAtOffsetAdjuster(1.35),
            NameGlobalInspectionGroupAdjuster(mutableMapOf<String, Double>("west" to 1.18, "retail" to 1.3)),
            RoundingGlobalInspectionGroupAdjuster(0.05),
            TieredGlobalInspectionGroupAdjuster(
                mutableListOf<Double>(6065.0, 7888.0, 8571.0),
                mutableListOf<Double>(1.04, 0.96, 0.83),
            ),
            FlaggedGlobalInspectionGroupAdjuster(flags, "global-inspection-group-scheduled", -4.4),
            CapGlobalInspectionGroupAdjuster(32456.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 37505.8,
    )
}

package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global inspection mapping calculation. */
interface GlobalInspectionMappingStep {
    fun describe(): String
    fun adjust(item: GlobalInspectionMapping, value: Double): Double
}

data class TieredGlobalInspectionMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalInspectionMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalInspectionMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalInspectionMappingRevisionOffsetAdjuster(var weight: Double) : GlobalInspectionMappingStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: GlobalInspectionMapping, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class NameGlobalInspectionMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalInspectionMappingStep {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalInspectionMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalInspectionMappingAdjuster(var cap: Double) : GlobalInspectionMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalInspectionMapping, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalInspectionMappingAdjuster(var step: Double) : GlobalInspectionMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalInspectionMapping, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalInspectionMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalInspectionMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalInspectionMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global inspection mapping steps in order and keeps a trace of each. */
data class GlobalInspectionMappingEngine(
    var steps: MutableList<GlobalInspectionMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalInspectionMapping): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalInspectionMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalInspectionMappingEngine(flags: Registry): GlobalInspectionMappingEngine {
    return GlobalInspectionMappingEngine(
        steps = mutableListOf<GlobalInspectionMappingStep>(
            TieredGlobalInspectionMappingAdjuster(
                mutableListOf<Double>(2896.0, 3214.0),
                mutableListOf<Double>(1.09, 0.89),
            ),
            GlobalInspectionMappingRevisionOffsetAdjuster(1.33),
            NameGlobalInspectionMappingAdjuster(mutableMapOf<String, Double>("north" to 1.17)),
            CapGlobalInspectionMappingAdjuster(27656.0),
            RoundingGlobalInspectionMappingAdjuster(0.05),
            FlaggedGlobalInspectionMappingAdjuster(flags, "global-inspection-mapping-weighted", 0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 43835.6,
    )
}

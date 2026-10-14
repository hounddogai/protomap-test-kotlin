package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global inspection policy calculation. */
interface GlobalInspectionPolicyStep {
    fun describe(): String
    fun adjust(item: GlobalInspectionPolicy, value: Double): Double
}

data class PercentGlobalInspectionPolicyAdjuster(var percent: Double) : GlobalInspectionPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalInspectionPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalInspectionPolicyAdjuster(var cap: Double) : GlobalInspectionPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalInspectionPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalInspectionPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalInspectionPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalInspectionPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalInspectionPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalInspectionPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalInspectionPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global inspection policy steps in order and keeps a trace of each. */
data class GlobalInspectionPolicyEngine(
    var steps: MutableList<GlobalInspectionPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalInspectionPolicy): Double {
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

    fun evaluateAll(items: MutableList<GlobalInspectionPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalInspectionPolicyEngine(flags: Registry): GlobalInspectionPolicyEngine {
    return GlobalInspectionPolicyEngine(
        steps = mutableListOf<GlobalInspectionPolicyStep>(
            PercentGlobalInspectionPolicyAdjuster(-20.7),
            CapGlobalInspectionPolicyAdjuster(33040.0),
            FlaggedGlobalInspectionPolicyAdjuster(flags, "global-inspection-policy-weighted", -8.2),
            TieredGlobalInspectionPolicyAdjuster(
                mutableListOf<Double>(1377.0, 8478.0, 8847.0, 9840.0),
                mutableListOf<Double>(0.96, 1.01, 0.91, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 3894.3,
    )
}

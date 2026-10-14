package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection policy calculation. */
interface InspectionPolicyFactor {
    fun describe(): String
    fun adjust(item: InspectionPolicy, value: Double): Double
}

data class InspectionPolicyFloorValueOffsetAdjuster(var weight: Double) : InspectionPolicyFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: InspectionPolicy, value: Double): Double = value + item.floorValue * weight
}

data class PercentInspectionPolicyAdjuster(var percent: Double) : InspectionPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelInspectionPolicyAdjuster(var factors: MutableMap<String, Double>) : InspectionPolicyFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection policy steps in order and keeps a trace of each. */
data class InspectionPolicyEngine(
    var steps: MutableList<InspectionPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionPolicy): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionPolicyEngine(): InspectionPolicyEngine {
    return InspectionPolicyEngine(
        steps = mutableListOf<InspectionPolicyFactor>(
            InspectionPolicyFloorValueOffsetAdjuster(1.81),
            PercentInspectionPolicyAdjuster(-15.4),
            LabelInspectionPolicyAdjuster(
                mutableMapOf<String, Double>("north" to 0.97, "east" to 1.35, "online" to 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 30263.0,
    )
}

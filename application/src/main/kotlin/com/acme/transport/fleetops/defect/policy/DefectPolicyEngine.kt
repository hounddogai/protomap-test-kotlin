package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the defect policy calculation. */
interface DefectPolicyStep {
    fun describe(): String
    fun adjust(item: DefectPolicy, value: Double): Double
}

data class RoundingDefectPolicyAdjuster(var step: Double) : DefectPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectPolicy, value: Double): Double = roundTo(value, step)
}

data class TitleDefectPolicyAdjuster(var factors: MutableMap<String, Double>) : DefectPolicyStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DefectPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefectPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefectPolicyAdjuster(var minimum: Double) : DefectPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the defect policy steps in order and keeps a trace of each. */
data class DefectPolicyEngine(
    var steps: MutableList<DefectPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefectPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefectPolicyEngine(): DefectPolicyEngine {
    return DefectPolicyEngine(
        steps = mutableListOf<DefectPolicyStep>(
            RoundingDefectPolicyAdjuster(0.05),
            TitleDefectPolicyAdjuster(mutableMapOf<String, Double>("online" to 1.0, "east" to 1.33)),
            TieredDefectPolicyAdjuster(mutableListOf<Double>(6782.0, 8858.0), mutableListOf<Double>(0.82, 1.11)),
            MinimumDefectPolicyAdjuster(18.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 73055.5,
    )
}

package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the violation plan calculation. */
interface ViolationPlanStep {
    fun describe(): String
    fun adjust(item: ViolationPlan, value: Double): Double
}

data class FlaggedViolationPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceViolationPlanAdjuster(var factors: MutableMap<String, Double>) : ViolationPlanStep {
    override fun describe(): String = "by source"

    override fun adjust(item: ViolationPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentViolationPlanAdjuster(var percent: Double) : ViolationPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumViolationPlanAdjuster(var minimum: Double) : ViolationPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the violation plan steps in order and keeps a trace of each. */
data class ViolationPlanEngine(
    var steps: MutableList<ViolationPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationPlan): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationPlanEngine(flags: Registry): ViolationPlanEngine {
    return ViolationPlanEngine(
        steps = mutableListOf<ViolationPlanStep>(
            FlaggedViolationPlanAdjuster(flags, "violation-plan-pending", 3.7),
            SourceViolationPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 0.94, "east" to 0.95, "north" to 0.72),
            ),
            PercentViolationPlanAdjuster(-4.7),
            MinimumViolationPlanAdjuster(48.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 9522.8,
    )
}

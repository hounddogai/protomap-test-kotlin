package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global service plan target calculation. */
interface GlobalServicePlanTargetStep {
    fun describe(): String
    fun adjust(item: GlobalServicePlanTarget, value: Double): Double
}

data class FlaggedGlobalServicePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalServicePlanTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalServicePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalServicePlanTargetAdjuster(var step: Double) : GlobalServicePlanTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalServicePlanTarget, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalServicePlanTargetAdjuster(var percent: Double) : GlobalServicePlanTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalServicePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalServicePlanTargetAmountOffsetAdjuster(var weight: Double) : GlobalServicePlanTargetStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: GlobalServicePlanTarget, value: Double): Double = value + item.amount * weight
}

data class DepotGlobalServicePlanTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalServicePlanTargetStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalServicePlanTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global service plan target steps in order and keeps a trace of each. */
data class GlobalServicePlanTargetEngine(
    var steps: MutableList<GlobalServicePlanTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalServicePlanTarget): Double {
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

    fun evaluateAll(items: MutableList<GlobalServicePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalServicePlanTargetEngine(flags: Registry): GlobalServicePlanTargetEngine {
    return GlobalServicePlanTargetEngine(
        steps = mutableListOf<GlobalServicePlanTargetStep>(
            FlaggedGlobalServicePlanTargetAdjuster(flags, "global-service-plan-target-primary", -4.7),
            RoundingGlobalServicePlanTargetAdjuster(0.1),
            PercentGlobalServicePlanTargetAdjuster(-1.3),
            GlobalServicePlanTargetAmountOffsetAdjuster(1.57),
            DepotGlobalServicePlanTargetAdjuster(mutableMapOf<String, Double>("east" to 1.14)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 5803.1,
    )
}

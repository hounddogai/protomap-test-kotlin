package com.acme.transport.fleetops.utilization.plan

/** One step of the fallback utilization plan calculation. */
interface FallbackUtilizationPlanFactor {
    fun describe(): String
    fun adjust(item: FallbackUtilizationPlan, value: Double): Double
}

data class LaneFallbackUtilizationPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackUtilizationPlanFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: FallbackUtilizationPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackUtilizationPlanBudgetOffsetAdjuster(var weight: Double) : FallbackUtilizationPlanFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: FallbackUtilizationPlan, value: Double): Double = value + item.budget * weight
}

data class MinimumFallbackUtilizationPlanAdjuster(var minimum: Double) : FallbackUtilizationPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackUtilizationPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback utilization plan steps in order and keeps a trace of each. */
data class FallbackUtilizationPlanEngine(
    var steps: MutableList<FallbackUtilizationPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackUtilizationPlan): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackUtilizationPlanFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackUtilizationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackUtilizationPlanEngine(): FallbackUtilizationPlanEngine {
    return FallbackUtilizationPlanEngine(
        steps = mutableListOf<FallbackUtilizationPlanFactor>(
            LaneFallbackUtilizationPlanAdjuster(mutableMapOf<String, Double>("north" to 0.77, "retail" to 0.94)),
            FallbackUtilizationPlanBudgetOffsetAdjuster(1.58),
            MinimumFallbackUtilizationPlanAdjuster(27.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 66731.5,
    )
}

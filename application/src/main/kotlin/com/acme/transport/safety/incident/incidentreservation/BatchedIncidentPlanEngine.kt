package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched incident plan calculation. */
interface BatchedIncidentPlanAdjuster {
    fun describe(): String
    fun adjust(item: BatchedIncidentPlan, value: Double): Double
}

data class TieredBatchedIncidentPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedIncidentPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedIncidentPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedIncidentPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedIncidentPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedIncidentPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedIncidentPlanAdjuster(var step: Double) : BatchedIncidentPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedIncidentPlan, value: Double): Double = roundTo(value, step)
}

data class BatchedIncidentPlanBudgetOffsetAdjuster(var weight: Double) : BatchedIncidentPlanAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: BatchedIncidentPlan, value: Double): Double = value + item.budget * weight
}

data class MinimumBatchedIncidentPlanAdjuster(var minimum: Double) : BatchedIncidentPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedIncidentPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedIncidentPlanAdjuster(var cap: Double) : BatchedIncidentPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedIncidentPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the batched incident plan steps in order and keeps a trace of each. */
data class BatchedIncidentPlanEngine(
    var steps: MutableList<BatchedIncidentPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedIncidentPlan): Double {
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

    fun evaluateAll(items: MutableList<BatchedIncidentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedIncidentPlanEngine(flags: Registry): BatchedIncidentPlanEngine {
    return BatchedIncidentPlanEngine(
        steps = mutableListOf<BatchedIncidentPlanAdjuster>(
            TieredBatchedIncidentPlanAdjuster(
                mutableListOf<Double>(1124.0, 3285.0, 8001.0, 9208.0),
                mutableListOf<Double>(1.14, 1.15, 1.1, 1.15),
            ),
            FlaggedBatchedIncidentPlanAdjuster(flags, "batched-incident-plan-primary", -0.8),
            RoundingBatchedIncidentPlanAdjuster(0.25),
            BatchedIncidentPlanBudgetOffsetAdjuster(1.39),
            MinimumBatchedIncidentPlanAdjuster(48.0),
            CapBatchedIncidentPlanAdjuster(27380.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 90333.5,
    )
}

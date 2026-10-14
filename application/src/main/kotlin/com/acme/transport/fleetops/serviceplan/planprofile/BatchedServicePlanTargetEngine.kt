package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched service plan target calculation. */
interface BatchedServicePlanTargetModifier {
    fun describe(): String
    fun adjust(item: BatchedServicePlanTarget, value: Double): Double
}

data class RoundingBatchedServicePlanTargetAdjuster(var step: Double) : BatchedServicePlanTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedServicePlanTarget, value: Double): Double = roundTo(value, step)
}

data class NameBatchedServicePlanTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedServicePlanTargetModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedServicePlanTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedServicePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedServicePlanTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedServicePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedServicePlanTargetUpdatedAtOffsetAdjuster(
    var weight: Double,
) : BatchedServicePlanTargetModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: BatchedServicePlanTarget, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class CapBatchedServicePlanTargetAdjuster(var cap: Double) : BatchedServicePlanTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedServicePlanTarget, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedServicePlanTargetAdjuster(var percent: Double) : BatchedServicePlanTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedServicePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched service plan target steps in order and keeps a trace of each. */
data class BatchedServicePlanTargetEngine(
    var steps: MutableList<BatchedServicePlanTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedServicePlanTarget): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedServicePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedServicePlanTargetEngine(flags: Registry): BatchedServicePlanTargetEngine {
    return BatchedServicePlanTargetEngine(
        steps = mutableListOf<BatchedServicePlanTargetModifier>(
            RoundingBatchedServicePlanTargetAdjuster(0.05),
            NameBatchedServicePlanTargetAdjuster(mutableMapOf<String, Double>("north" to 0.97, "east" to 1.14)),
            FlaggedBatchedServicePlanTargetAdjuster(flags, "batched-service-plan-target-global", 1.4),
            BatchedServicePlanTargetUpdatedAtOffsetAdjuster(1.44),
            CapBatchedServicePlanTargetAdjuster(15388.0),
            PercentBatchedServicePlanTargetAdjuster(-2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 2126.9,
    )
}

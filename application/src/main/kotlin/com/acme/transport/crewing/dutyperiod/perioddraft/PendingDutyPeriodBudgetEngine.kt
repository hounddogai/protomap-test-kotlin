package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending duty period budget calculation. */
interface PendingDutyPeriodBudgetModifier {
    fun describe(): String
    fun adjust(item: PendingDutyPeriodBudget, value: Double): Double
}

data class PendingDutyPeriodBudgetCostOffsetAdjuster(var weight: Double) : PendingDutyPeriodBudgetModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: PendingDutyPeriodBudget, value: Double): Double = value + item.cost * weight
}

data class TieredPendingDutyPeriodBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDutyPeriodBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDutyPeriodBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentPendingDutyPeriodBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDutyPeriodBudgetModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: PendingDutyPeriodBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingDutyPeriodBudgetAdjuster(var percent: Double) : PendingDutyPeriodBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDutyPeriodBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingDutyPeriodBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDutyPeriodBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDutyPeriodBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending duty period budget steps in order and keeps a trace of each. */
data class PendingDutyPeriodBudgetEngine(
    var steps: MutableList<PendingDutyPeriodBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingDutyPeriodBudget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDutyPeriodBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingDutyPeriodBudgetEngine(flags: Registry): PendingDutyPeriodBudgetEngine {
    return PendingDutyPeriodBudgetEngine(
        steps = mutableListOf<PendingDutyPeriodBudgetModifier>(
            PendingDutyPeriodBudgetCostOffsetAdjuster(1.98),
            TieredPendingDutyPeriodBudgetAdjuster(
                mutableListOf<Double>(149.0, 2460.0, 3085.0, 9322.0),
                mutableListOf<Double>(0.81, 1.13, 0.93, 0.91),
            ),
            SegmentPendingDutyPeriodBudgetAdjuster(
                mutableMapOf<String, Double>("south" to 0.96, "retail" to 1.15, "east" to 0.93),
            ),
            PercentPendingDutyPeriodBudgetAdjuster(-0.8),
            FlaggedPendingDutyPeriodBudgetAdjuster(flags, "pending-duty-period-budget-manual", 6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 6704.5,
    )
}

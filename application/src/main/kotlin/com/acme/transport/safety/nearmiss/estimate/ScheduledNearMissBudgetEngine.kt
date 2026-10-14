package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled near miss budget calculation. */
interface ScheduledNearMissBudgetAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledNearMissBudget, value: Double): Double
}

data class RoundingScheduledNearMissBudgetAdjuster(var step: Double) : ScheduledNearMissBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledNearMissBudget, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledNearMissBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledNearMissBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledNearMissBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledNearMissBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledNearMissBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledNearMissBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledNearMissBudgetAdjuster(var percent: Double) : ScheduledNearMissBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledNearMissBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled near miss budget steps in order and keeps a trace of each. */
data class ScheduledNearMissBudgetEngine(
    var steps: MutableList<ScheduledNearMissBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledNearMissBudget): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledNearMissBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledNearMissBudgetEngine(flags: Registry): ScheduledNearMissBudgetEngine {
    return ScheduledNearMissBudgetEngine(
        steps = mutableListOf<ScheduledNearMissBudgetAdjuster>(
            RoundingScheduledNearMissBudgetAdjuster(1.0),
            TieredScheduledNearMissBudgetAdjuster(
                mutableListOf<Double>(3149.0, 5945.0, 6857.0, 8094.0),
                mutableListOf<Double>(1.18, 0.93, 1.17, 1.06),
            ),
            FlaggedScheduledNearMissBudgetAdjuster(flags, "scheduled-near-miss-budget-rolling", -3.1),
            PercentScheduledNearMissBudgetAdjuster(-3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 28078.0,
    )
}

package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff budget calculation. */
interface CutoffBudgetFactor {
    fun describe(): String
    fun adjust(item: CutoffBudget, value: Double): Double
}

data class SourceCutoffBudgetAdjuster(var factors: MutableMap<String, Double>) : CutoffBudgetFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: CutoffBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCutoffBudgetAdjuster(var minimum: Double) : CutoffBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCutoffBudgetAdjuster(var step: Double) : CutoffBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffBudget, value: Double): Double = roundTo(value, step)
}

data class FlaggedCutoffBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCutoffBudgetAdjuster(var cap: Double) : CutoffBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the cutoff budget steps in order and keeps a trace of each. */
data class CutoffBudgetEngine(
    var steps: MutableList<CutoffBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CutoffBudget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCutoffBudgetEngine(flags: Registry): CutoffBudgetEngine {
    return CutoffBudgetEngine(
        steps = mutableListOf<CutoffBudgetFactor>(
            SourceCutoffBudgetAdjuster(
                mutableMapOf<String, Double>("east" to 0.97, "central" to 0.78, "online" to 0.96),
            ),
            MinimumCutoffBudgetAdjuster(47.7),
            RoundingCutoffBudgetAdjuster(1.0),
            FlaggedCutoffBudgetAdjuster(flags, "cutoff-budget-primary", 6.6),
            CapCutoffBudgetAdjuster(10736.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 54604.6,
    )
}

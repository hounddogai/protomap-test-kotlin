package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered tender budget calculation. */
interface TieredTenderBudgetFactor {
    fun describe(): String
    fun adjust(item: TieredTenderBudget, value: Double): Double
}

data class NameTieredTenderBudgetAdjuster(var factors: MutableMap<String, Double>) : TieredTenderBudgetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredTenderBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredTenderBudgetAdjuster(var minimum: Double) : TieredTenderBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTenderBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredTenderBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTenderBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTenderBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredTenderBudgetAdjuster(var cap: Double) : TieredTenderBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTenderBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered tender budget steps in order and keeps a trace of each. */
data class TieredTenderBudgetEngine(
    var steps: MutableList<TieredTenderBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredTenderBudget): Double {
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

    fun evaluateAll(items: MutableList<TieredTenderBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredTenderBudgetEngine(flags: Registry): TieredTenderBudgetEngine {
    return TieredTenderBudgetEngine(
        steps = mutableListOf<TieredTenderBudgetFactor>(
            NameTieredTenderBudgetAdjuster(
                mutableMapOf<String, Double>("central" to 0.8, "west" to 0.75, "north" to 0.85),
            ),
            MinimumTieredTenderBudgetAdjuster(22.4),
            FlaggedTieredTenderBudgetAdjuster(flags, "tiered-tender-budget-scheduled", -2.2),
            CapTieredTenderBudgetAdjuster(25220.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 44335.9,
    )
}

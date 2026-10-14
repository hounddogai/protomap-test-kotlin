package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect budget calculation. */
interface DefectBudgetModifier {
    fun describe(): String
    fun adjust(item: DefectBudget, value: Double): Double
}

data class PercentDefectBudgetAdjuster(var percent: Double) : DefectBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefectBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleDefectBudgetAdjuster(var factors: MutableMap<String, Double>) : DefectBudgetModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefectBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefectBudgetAdjuster(var cap: Double) : DefectBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the defect budget steps in order and keeps a trace of each. */
data class DefectBudgetEngine(
    var steps: MutableList<DefectBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefectBudget): Double {
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

    fun evaluateAll(items: MutableList<DefectBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefectBudgetEngine(): DefectBudgetEngine {
    return DefectBudgetEngine(
        steps = mutableListOf<DefectBudgetModifier>(
            PercentDefectBudgetAdjuster(-9.5),
            TieredDefectBudgetAdjuster(
                mutableListOf<Double>(374.0, 3269.0, 9185.0),
                mutableListOf<Double>(1.17, 0.95, 1.08),
            ),
            LocaleDefectBudgetAdjuster(mutableMapOf<String, Double>("south" to 0.88)),
            CapDefectBudgetAdjuster(47143.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 66949.3,
    )
}

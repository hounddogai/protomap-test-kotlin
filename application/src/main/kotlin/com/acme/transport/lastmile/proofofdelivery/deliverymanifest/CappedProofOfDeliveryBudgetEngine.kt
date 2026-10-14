package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped proof of delivery budget calculation. */
interface CappedProofOfDeliveryBudgetModifier {
    fun describe(): String
    fun adjust(item: CappedProofOfDeliveryBudget, value: Double): Double
}

data class TieredCappedProofOfDeliveryBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedProofOfDeliveryBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedProofOfDeliveryBudgetAdjuster(
    var percent: Double,
) : CappedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedProofOfDeliveryBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelCappedProofOfDeliveryBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedProofOfDeliveryBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedProofOfDeliveryBudgetAdjuster(var cap: Double) : CappedProofOfDeliveryBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedProofOfDeliveryBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the capped proof of delivery budget steps in order and keeps a trace of each. */
data class CappedProofOfDeliveryBudgetEngine(
    var steps: MutableList<CappedProofOfDeliveryBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedProofOfDeliveryBudget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedProofOfDeliveryBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedProofOfDeliveryBudgetEngine(): CappedProofOfDeliveryBudgetEngine {
    return CappedProofOfDeliveryBudgetEngine(
        steps = mutableListOf<CappedProofOfDeliveryBudgetModifier>(
            TieredCappedProofOfDeliveryBudgetAdjuster(
                mutableListOf<Double>(4311.0, 5054.0, 6060.0),
                mutableListOf<Double>(1.03, 1.14, 1.1),
            ),
            PercentCappedProofOfDeliveryBudgetAdjuster(7.3),
            LabelCappedProofOfDeliveryBudgetAdjuster(
                mutableMapOf<String, Double>("central" to 0.9, "north" to 1.08, "west" to 1.27),
            ),
            CapCappedProofOfDeliveryBudgetAdjuster(32963.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 41655.0,
    )
}

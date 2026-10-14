package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery budget calculation. */
interface ProofOfDeliveryBudgetModifier {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryBudget, value: Double): Double
}

data class NameProofOfDeliveryBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryBudgetModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ProofOfDeliveryBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingProofOfDeliveryBudgetAdjuster(var step: Double) : ProofOfDeliveryBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryBudget, value: Double): Double = roundTo(value, step)
}

data class PercentProofOfDeliveryBudgetAdjuster(var percent: Double) : ProofOfDeliveryBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedProofOfDeliveryBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredProofOfDeliveryBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumProofOfDeliveryBudgetAdjuster(var minimum: Double) : ProofOfDeliveryBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the proof of delivery budget steps in order and keeps a trace of each. */
data class ProofOfDeliveryBudgetEngine(
    var steps: MutableList<ProofOfDeliveryBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ProofOfDeliveryBudget): Double {
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryBudgetEngine(flags: Registry): ProofOfDeliveryBudgetEngine {
    return ProofOfDeliveryBudgetEngine(
        steps = mutableListOf<ProofOfDeliveryBudgetModifier>(
            NameProofOfDeliveryBudgetAdjuster(
                mutableMapOf<String, Double>("central" to 1.19, "north" to 0.76, "online" to 1.2),
            ),
            RoundingProofOfDeliveryBudgetAdjuster(0.5),
            PercentProofOfDeliveryBudgetAdjuster(-2.1),
            FlaggedProofOfDeliveryBudgetAdjuster(flags, "proof-of-delivery-budget-scheduled", -6.9),
            TieredProofOfDeliveryBudgetAdjuster(
                mutableListOf<Double>(5574.0, 6188.0),
                mutableListOf<Double>(1.11, 1.15),
            ),
            MinimumProofOfDeliveryBudgetAdjuster(14.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 53381.5,
    )
}

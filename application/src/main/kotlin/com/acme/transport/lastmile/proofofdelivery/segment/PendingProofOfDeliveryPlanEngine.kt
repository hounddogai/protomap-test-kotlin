package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending proof of delivery plan calculation. */
interface PendingProofOfDeliveryPlanModifier {
    fun describe(): String
    fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double
}

data class CapPendingProofOfDeliveryPlanAdjuster(var cap: Double) : PendingProofOfDeliveryPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double = minOf(value, cap)
}

data class PercentPendingProofOfDeliveryPlanAdjuster(var percent: Double) : PendingProofOfDeliveryPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingProofOfDeliveryPlanAdjuster(var minimum: Double) : PendingProofOfDeliveryPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPendingProofOfDeliveryPlanAdjuster(var step: Double) : PendingProofOfDeliveryPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double = roundTo(value, step)
}

data class UnitPendingProofOfDeliveryPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingProofOfDeliveryPlanModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PendingProofOfDeliveryPlanRatioOffsetAdjuster(
    var weight: Double,
) : PendingProofOfDeliveryPlanModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PendingProofOfDeliveryPlan, value: Double): Double = value + item.ratio * weight
}

/** Runs the pending proof of delivery plan steps in order and keeps a trace of each. */
data class PendingProofOfDeliveryPlanEngine(
    var steps: MutableList<PendingProofOfDeliveryPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingProofOfDeliveryPlan): Double {
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

    fun evaluateAll(items: MutableList<PendingProofOfDeliveryPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingProofOfDeliveryPlanEngine(): PendingProofOfDeliveryPlanEngine {
    return PendingProofOfDeliveryPlanEngine(
        steps = mutableListOf<PendingProofOfDeliveryPlanModifier>(
            CapPendingProofOfDeliveryPlanAdjuster(6104.0),
            PercentPendingProofOfDeliveryPlanAdjuster(24.6),
            MinimumPendingProofOfDeliveryPlanAdjuster(40.5),
            RoundingPendingProofOfDeliveryPlanAdjuster(0.1),
            UnitPendingProofOfDeliveryPlanAdjuster(mutableMapOf<String, Double>("online" to 1.15)),
            PendingProofOfDeliveryPlanRatioOffsetAdjuster(0.22),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 83775.7,
    )
}

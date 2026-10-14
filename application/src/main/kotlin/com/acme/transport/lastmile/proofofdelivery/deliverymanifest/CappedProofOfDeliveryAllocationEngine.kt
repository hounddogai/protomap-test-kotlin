package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped proof of delivery allocation calculation. */
interface CappedProofOfDeliveryAllocationModifier {
    fun describe(): String
    fun adjust(item: CappedProofOfDeliveryAllocation, value: Double): Double
}

data class MinimumCappedProofOfDeliveryAllocationAdjuster(
    var minimum: Double,
) : CappedProofOfDeliveryAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedProofOfDeliveryAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedProofOfDeliveryAllocationAdjuster(
    var step: Double,
) : CappedProofOfDeliveryAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedProofOfDeliveryAllocation, value: Double): Double = roundTo(value, step)
}

data class CappedProofOfDeliveryAllocationStartsAtOffsetAdjuster(
    var weight: Double,
) : CappedProofOfDeliveryAllocationModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: CappedProofOfDeliveryAllocation, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

/** Runs the capped proof of delivery allocation steps in order and keeps a trace of each. */
data class CappedProofOfDeliveryAllocationEngine(
    var steps: MutableList<CappedProofOfDeliveryAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedProofOfDeliveryAllocation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedProofOfDeliveryAllocationModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedProofOfDeliveryAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedProofOfDeliveryAllocationEngine(): CappedProofOfDeliveryAllocationEngine {
    return CappedProofOfDeliveryAllocationEngine(
        steps = mutableListOf<CappedProofOfDeliveryAllocationModifier>(
            MinimumCappedProofOfDeliveryAllocationAdjuster(8.8),
            RoundingCappedProofOfDeliveryAllocationAdjuster(5.0),
            CappedProofOfDeliveryAllocationStartsAtOffsetAdjuster(0.18),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 5074.3,
    )
}

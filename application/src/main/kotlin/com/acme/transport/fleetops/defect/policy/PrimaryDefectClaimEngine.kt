package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary defect claim calculation. */
interface PrimaryDefectClaimAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDefectClaim, value: Double): Double
}

data class RoundingPrimaryDefectClaimAdjuster(var step: Double) : PrimaryDefectClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDefectClaim, value: Double): Double = roundTo(value, step)
}

data class PrimaryDefectClaimFuelLitersOffsetAdjuster(var weight: Double) : PrimaryDefectClaimAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: PrimaryDefectClaim, value: Double): Double = value + item.fuelLiters * weight
}

data class MinimumPrimaryDefectClaimAdjuster(var minimum: Double) : PrimaryDefectClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDefectClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary defect claim steps in order and keeps a trace of each. */
data class PrimaryDefectClaimEngine(
    var steps: MutableList<PrimaryDefectClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryDefectClaim): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDefectClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDefectClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryDefectClaimEngine(): PrimaryDefectClaimEngine {
    return PrimaryDefectClaimEngine(
        steps = mutableListOf<PrimaryDefectClaimAdjuster>(
            RoundingPrimaryDefectClaimAdjuster(1.0),
            PrimaryDefectClaimFuelLitersOffsetAdjuster(1.28),
            MinimumPrimaryDefectClaimAdjuster(12.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 33949.3,
    )
}

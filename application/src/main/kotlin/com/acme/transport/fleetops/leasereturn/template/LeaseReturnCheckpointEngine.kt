package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return checkpoint calculation. */
interface LeaseReturnCheckpointModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnCheckpoint, value: Double): Double
}

data class CountryLeaseReturnCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : LeaseReturnCheckpointModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LeaseReturnCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLeaseReturnCheckpointAdjuster(var cap: Double) : LeaseReturnCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LeaseReturnCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingLeaseReturnCheckpointAdjuster(var step: Double) : LeaseReturnCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return checkpoint steps in order and keeps a trace of each. */
data class LeaseReturnCheckpointEngine(
    var steps: MutableList<LeaseReturnCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LeaseReturnCheckpoint): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLeaseReturnCheckpointEngine(): LeaseReturnCheckpointEngine {
    return LeaseReturnCheckpointEngine(
        steps = mutableListOf<LeaseReturnCheckpointModifier>(
            CountryLeaseReturnCheckpointAdjuster(mutableMapOf<String, Double>("central" to 1.24)),
            CapLeaseReturnCheckpointAdjuster(21219.0),
            RoundingLeaseReturnCheckpointAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 55065.4,
    )
}

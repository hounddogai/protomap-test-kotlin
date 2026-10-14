package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return target calculation. */
interface LeaseReturnTargetModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnTarget, value: Double): Double
}

data class TitleLeaseReturnTargetAdjuster(var factors: MutableMap<String, Double>) : LeaseReturnTargetModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LeaseReturnTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLeaseReturnTargetAdjuster(var step: Double) : LeaseReturnTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnTarget, value: Double): Double = roundTo(value, step)
}

data class LeaseReturnTargetExpiresAtOffsetAdjuster(var weight: Double) : LeaseReturnTargetModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: LeaseReturnTarget, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

/** Runs the lease return target steps in order and keeps a trace of each. */
data class LeaseReturnTargetEngine(
    var steps: MutableList<LeaseReturnTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnTarget): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnTargetEngine(): LeaseReturnTargetEngine {
    return LeaseReturnTargetEngine(
        steps = mutableListOf<LeaseReturnTargetModifier>(
            TitleLeaseReturnTargetAdjuster(mutableMapOf<String, Double>("east" to 1.01)),
            RoundingLeaseReturnTargetAdjuster(5.0),
            LeaseReturnTargetExpiresAtOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 50732.8,
    )
}

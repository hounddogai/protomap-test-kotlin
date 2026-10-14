package com.acme.transport.network.node.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary node variance calculation. */
interface PrimaryNodeVarianceStep {
    fun describe(): String
    fun adjust(item: PrimaryNodeVariance, value: Double): Double
}

data class RoundingPrimaryNodeVarianceAdjuster(var step: Double) : PrimaryNodeVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryNodeVariance, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryNodeVarianceAdjuster(var cap: Double) : PrimaryNodeVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryNodeVariance, value: Double): Double = minOf(value, cap)
}

data class PrimaryNodeVarianceCapacityOffsetAdjuster(var weight: Double) : PrimaryNodeVarianceStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: PrimaryNodeVariance, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the primary node variance steps in order and keeps a trace of each. */
data class PrimaryNodeVarianceEngine(
    var steps: MutableList<PrimaryNodeVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryNodeVariance): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryNodeVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryNodeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryNodeVarianceEngine(): PrimaryNodeVarianceEngine {
    return PrimaryNodeVarianceEngine(
        steps = mutableListOf<PrimaryNodeVarianceStep>(
            RoundingPrimaryNodeVarianceAdjuster(5.0),
            CapPrimaryNodeVarianceAdjuster(18965.0),
            PrimaryNodeVarianceCapacityOffsetAdjuster(1.32),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 97832.9,
    )
}

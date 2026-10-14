package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending camera event proposal calculation. */
interface PendingCameraEventProposalStep {
    fun describe(): String
    fun adjust(item: PendingCameraEventProposal, value: Double): Double
}

data class RoundingPendingCameraEventProposalAdjuster(var step: Double) : PendingCameraEventProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCameraEventProposal, value: Double): Double = roundTo(value, step)
}

data class PendingCameraEventProposalCapacityOffsetAdjuster(
    var weight: Double,
) : PendingCameraEventProposalStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: PendingCameraEventProposal, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class MinimumPendingCameraEventProposalAdjuster(var minimum: Double) : PendingCameraEventProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCameraEventProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingCameraEventProposalAdjuster(var cap: Double) : PendingCameraEventProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCameraEventProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the pending camera event proposal steps in order and keeps a trace of each. */
data class PendingCameraEventProposalEngine(
    var steps: MutableList<PendingCameraEventProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingCameraEventProposal): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCameraEventProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCameraEventProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingCameraEventProposalEngine(): PendingCameraEventProposalEngine {
    return PendingCameraEventProposalEngine(
        steps = mutableListOf<PendingCameraEventProposalStep>(
            RoundingPendingCameraEventProposalAdjuster(0.05),
            PendingCameraEventProposalCapacityOffsetAdjuster(0.87),
            MinimumPendingCameraEventProposalAdjuster(29.4),
            CapPendingCameraEventProposalAdjuster(30930.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 20952.7,
    )
}

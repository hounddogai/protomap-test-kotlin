package com.acme.transport.linehaul.capacity.capacitycycle

/** One step of the scheduled capacity proposal calculation. */
interface ScheduledCapacityProposalStep {
    fun describe(): String
    fun adjust(item: ScheduledCapacityProposal, value: Double): Double
}

data class TieredScheduledCapacityProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCapacityProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCapacityProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceScheduledCapacityProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledCapacityProposalStep {
    override fun describe(): String = "by source"

    override fun adjust(item: ScheduledCapacityProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledCapacityProposalAdjuster(var cap: Double) : ScheduledCapacityProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledCapacityProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledCapacityProposalAdjuster(var minimum: Double) : ScheduledCapacityProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledCapacityProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled capacity proposal steps in order and keeps a trace of each. */
data class ScheduledCapacityProposalEngine(
    var steps: MutableList<ScheduledCapacityProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledCapacityProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledCapacityProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledCapacityProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledCapacityProposalEngine(): ScheduledCapacityProposalEngine {
    return ScheduledCapacityProposalEngine(
        steps = mutableListOf<ScheduledCapacityProposalStep>(
            TieredScheduledCapacityProposalAdjuster(
                mutableListOf<Double>(1730.0, 4192.0, 4886.0, 5410.0),
                mutableListOf<Double>(0.88, 0.81, 1.01, 1.01),
            ),
            SourceScheduledCapacityProposalAdjuster(
                mutableMapOf<String, Double>("retail" to 0.76, "central" to 0.78),
            ),
            CapScheduledCapacityProposalAdjuster(29588.0),
            MinimumScheduledCapacityProposalAdjuster(45.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 42038.7,
    )
}

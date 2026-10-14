package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the idling proposal calculation. */
interface IdlingProposalStep {
    fun describe(): String
    fun adjust(item: IdlingProposal, value: Double): Double
}

data class PercentIdlingProposalAdjuster(var percent: Double) : IdlingProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingIdlingProposalAdjuster(var step: Double) : IdlingProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingProposal, value: Double): Double = roundTo(value, step)
}

data class TieredIdlingProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the idling proposal steps in order and keeps a trace of each. */
data class IdlingProposalEngine(
    var steps: MutableList<IdlingProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingProposal): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingProposalEngine(): IdlingProposalEngine {
    return IdlingProposalEngine(
        steps = mutableListOf<IdlingProposalStep>(
            PercentIdlingProposalAdjuster(-13.9),
            RoundingIdlingProposalAdjuster(1.0),
            TieredIdlingProposalAdjuster(
                mutableListOf<Double>(7527.0, 8426.0, 8649.0),
                mutableListOf<Double>(1.19, 1.03, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 27530.2,
    )
}

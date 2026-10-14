package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict roster agreement calculation. */
interface StrictRosterAgreementFactor {
    fun describe(): String
    fun adjust(item: StrictRosterAgreement, value: Double): Double
}

data class RoundingStrictRosterAgreementAdjuster(var step: Double) : StrictRosterAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRosterAgreement, value: Double): Double = roundTo(value, step)
}

data class TieredStrictRosterAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRosterAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRosterAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictRosterAgreementAdjuster(var minimum: Double) : StrictRosterAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRosterAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictRosterAgreementAdjuster(var cap: Double) : StrictRosterAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRosterAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the strict roster agreement steps in order and keeps a trace of each. */
data class StrictRosterAgreementEngine(
    var steps: MutableList<StrictRosterAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictRosterAgreement): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRosterAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRosterAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictRosterAgreementEngine(): StrictRosterAgreementEngine {
    return StrictRosterAgreementEngine(
        steps = mutableListOf<StrictRosterAgreementFactor>(
            RoundingStrictRosterAgreementAdjuster(5.0),
            TieredStrictRosterAgreementAdjuster(
                mutableListOf<Double>(20.0, 2152.0, 9261.0),
                mutableListOf<Double>(1.01, 1.17, 0.82),
            ),
            MinimumStrictRosterAgreementAdjuster(24.7),
            CapStrictRosterAgreementAdjuster(11632.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 88160.7,
    )
}

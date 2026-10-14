package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default roster agreement calculation. */
interface DefaultRosterAgreementFactor {
    fun describe(): String
    fun adjust(item: DefaultRosterAgreement, value: Double): Double
}

data class RoundingDefaultRosterAgreementAdjuster(var step: Double) : DefaultRosterAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRosterAgreement, value: Double): Double = roundTo(value, step)
}

data class CapDefaultRosterAgreementAdjuster(var cap: Double) : DefaultRosterAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRosterAgreement, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultRosterAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRosterAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRosterAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefaultRosterAgreementAdjuster(var percent: Double) : DefaultRosterAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRosterAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default roster agreement steps in order and keeps a trace of each. */
data class DefaultRosterAgreementEngine(
    var steps: MutableList<DefaultRosterAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultRosterAgreement): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultRosterAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultRosterAgreementEngine(): DefaultRosterAgreementEngine {
    return DefaultRosterAgreementEngine(
        steps = mutableListOf<DefaultRosterAgreementFactor>(
            RoundingDefaultRosterAgreementAdjuster(5.0),
            CapDefaultRosterAgreementAdjuster(36576.0),
            TieredDefaultRosterAgreementAdjuster(
                mutableListOf<Double>(1905.0, 7563.0),
                mutableListOf<Double>(0.91, 1.02),
            ),
            PercentDefaultRosterAgreementAdjuster(-5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 91986.5,
    )
}

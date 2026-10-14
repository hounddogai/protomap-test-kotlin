package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the default qualification agreement calculation. */
interface DefaultQualificationAgreementStep {
    fun describe(): String
    fun adjust(item: DefaultQualificationAgreement, value: Double): Double
}

data class DefaultQualificationAgreementMinimumOffsetAdjuster(
    var weight: Double,
) : DefaultQualificationAgreementStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DefaultQualificationAgreement, value: Double): Double = value + item.minimum * weight
}

data class PercentDefaultQualificationAgreementAdjuster(
    var percent: Double,
) : DefaultQualificationAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultQualificationAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultQualificationAgreementAdjuster(var step: Double) : DefaultQualificationAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultQualificationAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the default qualification agreement steps in order and keeps a trace of each. */
data class DefaultQualificationAgreementEngine(
    var steps: MutableList<DefaultQualificationAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultQualificationAgreement): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultQualificationAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultQualificationAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultQualificationAgreementEngine(): DefaultQualificationAgreementEngine {
    return DefaultQualificationAgreementEngine(
        steps = mutableListOf<DefaultQualificationAgreementStep>(
            DefaultQualificationAgreementMinimumOffsetAdjuster(0.46),
            PercentDefaultQualificationAgreementAdjuster(0.1),
            RoundingDefaultQualificationAgreementAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 12707.5,
    )
}

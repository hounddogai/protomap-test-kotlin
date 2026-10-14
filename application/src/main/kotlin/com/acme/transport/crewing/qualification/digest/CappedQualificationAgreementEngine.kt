package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped qualification agreement calculation. */
interface CappedQualificationAgreementStep {
    fun describe(): String
    fun adjust(item: CappedQualificationAgreement, value: Double): Double
}

data class NameCappedQualificationAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedQualificationAgreementStep {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedQualificationAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedQualificationAgreementVersionOffsetAdjuster(
    var weight: Double,
) : CappedQualificationAgreementStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: CappedQualificationAgreement, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class RoundingCappedQualificationAgreementAdjuster(var step: Double) : CappedQualificationAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedQualificationAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedQualificationAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedQualificationAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedQualificationAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped qualification agreement steps in order and keeps a trace of each. */
data class CappedQualificationAgreementEngine(
    var steps: MutableList<CappedQualificationAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedQualificationAgreement): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedQualificationAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedQualificationAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedQualificationAgreementEngine(flags: Registry): CappedQualificationAgreementEngine {
    return CappedQualificationAgreementEngine(
        steps = mutableListOf<CappedQualificationAgreementStep>(
            NameCappedQualificationAgreementAdjuster(
                mutableMapOf<String, Double>("south" to 0.97, "east" to 1.28),
            ),
            CappedQualificationAgreementVersionOffsetAdjuster(1.28),
            RoundingCappedQualificationAgreementAdjuster(0.25),
            FlaggedCappedQualificationAgreementAdjuster(flags, "capped-qualification-agreement-capped", -8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 74261.8,
    )
}

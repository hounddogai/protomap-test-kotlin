package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification agreement calculation. */
interface QualificationAgreementModifier {
    fun describe(): String
    fun adjust(item: QualificationAgreement, value: Double): Double
}

data class SourceQualificationAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationAgreementModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: QualificationAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapQualificationAgreementAdjuster(var cap: Double) : QualificationAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationAgreement, value: Double): Double = minOf(value, cap)
}

data class PercentQualificationAgreementAdjuster(var percent: Double) : QualificationAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class QualificationAgreementRatioOffsetAdjuster(var weight: Double) : QualificationAgreementModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: QualificationAgreement, value: Double): Double = value + item.ratio * weight
}

data class RoundingQualificationAgreementAdjuster(var step: Double) : QualificationAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the qualification agreement steps in order and keeps a trace of each. */
data class QualificationAgreementEngine(
    var steps: MutableList<QualificationAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: QualificationAgreement): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultQualificationAgreementEngine(): QualificationAgreementEngine {
    return QualificationAgreementEngine(
        steps = mutableListOf<QualificationAgreementModifier>(
            SourceQualificationAgreementAdjuster(mutableMapOf<String, Double>("north" to 0.82)),
            CapQualificationAgreementAdjuster(32455.0),
            PercentQualificationAgreementAdjuster(12.0),
            QualificationAgreementRatioOffsetAdjuster(1.64),
            RoundingQualificationAgreementAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 74518.5,
    )
}

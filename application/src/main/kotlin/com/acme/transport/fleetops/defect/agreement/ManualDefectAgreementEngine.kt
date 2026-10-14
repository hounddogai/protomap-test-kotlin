package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual defect agreement calculation. */
interface ManualDefectAgreementModifier {
    fun describe(): String
    fun adjust(item: ManualDefectAgreement, value: Double): Double
}

data class RoundingManualDefectAgreementAdjuster(var step: Double) : ManualDefectAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDefectAgreement, value: Double): Double = roundTo(value, step)
}

data class LocaleManualDefectAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDefectAgreementModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualDefectAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualDefectAgreementAdjuster(var percent: Double) : ManualDefectAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDefectAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualDefectAgreementAdjuster(var minimum: Double) : ManualDefectAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDefectAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual defect agreement steps in order and keeps a trace of each. */
data class ManualDefectAgreementEngine(
    var steps: MutableList<ManualDefectAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDefectAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDefectAgreementModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDefectAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDefectAgreementEngine(): ManualDefectAgreementEngine {
    return ManualDefectAgreementEngine(
        steps = mutableListOf<ManualDefectAgreementModifier>(
            RoundingManualDefectAgreementAdjuster(0.25),
            LocaleManualDefectAgreementAdjuster(mutableMapOf<String, Double>("south" to 1.27, "east" to 1.13)),
            PercentManualDefectAgreementAdjuster(7.4),
            MinimumManualDefectAgreementAdjuster(37.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 85171.3,
    )
}

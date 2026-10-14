package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual roster agreement calculation. */
interface ManualRosterAgreementFactor {
    fun describe(): String
    fun adjust(item: ManualRosterAgreement, value: Double): Double
}

data class LabelManualRosterAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRosterAgreementFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualRosterAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedManualRosterAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRosterAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRosterAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualRosterAgreementAdjuster(var cap: Double) : ManualRosterAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualRosterAgreement, value: Double): Double = minOf(value, cap)
}

data class RoundingManualRosterAgreementAdjuster(var step: Double) : ManualRosterAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRosterAgreement, value: Double): Double = roundTo(value, step)
}

data class ManualRosterAgreementStartsAtOffsetAdjuster(var weight: Double) : ManualRosterAgreementFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ManualRosterAgreement, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class TieredManualRosterAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRosterAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRosterAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual roster agreement steps in order and keeps a trace of each. */
data class ManualRosterAgreementEngine(
    var steps: MutableList<ManualRosterAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualRosterAgreement): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualRosterAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualRosterAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualRosterAgreementEngine(flags: Registry): ManualRosterAgreementEngine {
    return ManualRosterAgreementEngine(
        steps = mutableListOf<ManualRosterAgreementFactor>(
            LabelManualRosterAgreementAdjuster(mutableMapOf<String, Double>("west" to 0.72)),
            FlaggedManualRosterAgreementAdjuster(flags, "manual-roster-agreement-strict", 7.6),
            CapManualRosterAgreementAdjuster(19743.0),
            RoundingManualRosterAgreementAdjuster(0.1),
            ManualRosterAgreementStartsAtOffsetAdjuster(1.08),
            TieredManualRosterAgreementAdjuster(
                mutableListOf<Double>(1904.0, 6736.0, 8358.0),
                mutableListOf<Double>(0.97, 0.97, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 51336.6,
    )
}

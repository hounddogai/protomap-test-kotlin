package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tire agreement calculation. */
interface ManualTireAgreementModifier {
    fun describe(): String
    fun adjust(item: ManualTireAgreement, value: Double): Double
}

data class ManualTireAgreementAmountOffsetAdjuster(var weight: Double) : ManualTireAgreementModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ManualTireAgreement, value: Double): Double = value + item.amount * weight
}

data class TieredManualTireAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTireAgreementModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTireAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualTireAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTireAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTireAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualTireAgreementAdjuster(var step: Double) : ManualTireAgreementModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTireAgreement, value: Double): Double = roundTo(value, step)
}

data class PercentManualTireAgreementAdjuster(var percent: Double) : ManualTireAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTireAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneManualTireAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTireAgreementModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualTireAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual tire agreement steps in order and keeps a trace of each. */
data class ManualTireAgreementEngine(
    var steps: MutableList<ManualTireAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTireAgreement): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTireAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTireAgreementEngine(flags: Registry): ManualTireAgreementEngine {
    return ManualTireAgreementEngine(
        steps = mutableListOf<ManualTireAgreementModifier>(
            ManualTireAgreementAmountOffsetAdjuster(0.78),
            TieredManualTireAgreementAdjuster(
                mutableListOf<Double>(153.0, 4230.0, 5363.0, 7149.0),
                mutableListOf<Double>(0.8, 1.05, 1.01, 1.19),
            ),
            FlaggedManualTireAgreementAdjuster(flags, "manual-tire-agreement-strict", 3.1),
            RoundingManualTireAgreementAdjuster(0.25),
            PercentManualTireAgreementAdjuster(-9.1),
            LaneManualTireAgreementAdjuster(
                mutableMapOf<String, Double>("online" to 1.37, "north" to 1.05, "west" to 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 34010.3,
    )
}

package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.flags.Registry

/** One step of the inspection agreement calculation. */
interface InspectionAgreementFactor {
    fun describe(): String
    fun adjust(item: InspectionAgreement, value: Double): Double
}

data class FlaggedInspectionAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredInspectionAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapInspectionAgreementAdjuster(var cap: Double) : InspectionAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumInspectionAgreementAdjuster(var minimum: Double) : InspectionAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection agreement steps in order and keeps a trace of each. */
data class InspectionAgreementEngine(
    var steps: MutableList<InspectionAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionAgreement): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionAgreementEngine(flags: Registry): InspectionAgreementEngine {
    return InspectionAgreementEngine(
        steps = mutableListOf<InspectionAgreementFactor>(
            FlaggedInspectionAgreementAdjuster(flags, "inspection-agreement-capped", -6.9),
            TieredInspectionAgreementAdjuster(
                mutableListOf<Double>(1316.0, 6380.0, 8156.0, 8934.0),
                mutableListOf<Double>(1.14, 0.81, 0.88, 1.09),
            ),
            CapInspectionAgreementAdjuster(44083.0),
            MinimumInspectionAgreementAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 85379.7,
    )
}

package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional incident agreement calculation. */
interface RegionalIncidentAgreementStep {
    fun describe(): String
    fun adjust(item: RegionalIncidentAgreement, value: Double): Double
}

data class CapRegionalIncidentAgreementAdjuster(var cap: Double) : RegionalIncidentAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalIncidentAgreement, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalIncidentAgreementAdjuster(var step: Double) : RegionalIncidentAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalIncidentAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalIncidentAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIncidentAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIncidentAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional incident agreement steps in order and keeps a trace of each. */
data class RegionalIncidentAgreementEngine(
    var steps: MutableList<RegionalIncidentAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalIncidentAgreement): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIncidentAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIncidentAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalIncidentAgreementEngine(flags: Registry): RegionalIncidentAgreementEngine {
    return RegionalIncidentAgreementEngine(
        steps = mutableListOf<RegionalIncidentAgreementStep>(
            CapRegionalIncidentAgreementAdjuster(9591.0),
            RoundingRegionalIncidentAgreementAdjuster(1.0),
            FlaggedRegionalIncidentAgreementAdjuster(flags, "regional-incident-agreement-tiered", 0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 8314.3,
    )
}

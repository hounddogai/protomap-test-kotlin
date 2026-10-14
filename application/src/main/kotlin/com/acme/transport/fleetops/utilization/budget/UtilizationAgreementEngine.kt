package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization agreement calculation. */
interface UtilizationAgreementModifier {
    fun describe(): String
    fun adjust(item: UtilizationAgreement, value: Double): Double
}

data class PercentUtilizationAgreementAdjuster(var percent: Double) : UtilizationAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumUtilizationAgreementAdjuster(var minimum: Double) : UtilizationAgreementModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedUtilizationAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the utilization agreement steps in order and keeps a trace of each. */
data class UtilizationAgreementEngine(
    var steps: MutableList<UtilizationAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationAgreement): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationAgreementEngine(flags: Registry): UtilizationAgreementEngine {
    return UtilizationAgreementEngine(
        steps = mutableListOf<UtilizationAgreementModifier>(
            PercentUtilizationAgreementAdjuster(21.9),
            MinimumUtilizationAgreementAdjuster(24.4),
            FlaggedUtilizationAgreementAdjuster(flags, "utilization-agreement-global", 2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 3920.2,
    )
}

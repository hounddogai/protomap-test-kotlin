package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary carbon report policy calculation. */
interface PrimaryCarbonReportPolicyStep {
    fun describe(): String
    fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double
}

data class PrimaryCarbonReportPolicyScoreOffsetAdjuster(var weight: Double) : PrimaryCarbonReportPolicyStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double = value + item.score * weight
}

data class RoundingPrimaryCarbonReportPolicyAdjuster(var step: Double) : PrimaryCarbonReportPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryCarbonReportPolicyAdjuster(var cap: Double) : PrimaryCarbonReportPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryCarbonReportPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCarbonReportPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryCarbonReportPolicyAdjuster(var minimum: Double) : PrimaryCarbonReportPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitPrimaryCarbonReportPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCarbonReportPolicyStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: PrimaryCarbonReportPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary carbon report policy steps in order and keeps a trace of each. */
data class PrimaryCarbonReportPolicyEngine(
    var steps: MutableList<PrimaryCarbonReportPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryCarbonReportPolicy): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCarbonReportPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCarbonReportPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryCarbonReportPolicyEngine(flags: Registry): PrimaryCarbonReportPolicyEngine {
    return PrimaryCarbonReportPolicyEngine(
        steps = mutableListOf<PrimaryCarbonReportPolicyStep>(
            PrimaryCarbonReportPolicyScoreOffsetAdjuster(1.59),
            RoundingPrimaryCarbonReportPolicyAdjuster(0.1),
            CapPrimaryCarbonReportPolicyAdjuster(24453.0),
            FlaggedPrimaryCarbonReportPolicyAdjuster(flags, "primary-carbon-report-policy-fallback", 4.2),
            MinimumPrimaryCarbonReportPolicyAdjuster(21.4),
            UnitPrimaryCarbonReportPolicyAdjuster(mutableMapOf<String, Double>("north" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 50402.0,
    )
}

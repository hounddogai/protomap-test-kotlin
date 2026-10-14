package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global carbon report policy calculation. */
interface GlobalCarbonReportPolicyModifier {
    fun describe(): String
    fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double
}

data class PercentGlobalCarbonReportPolicyAdjuster(var percent: Double) : GlobalCarbonReportPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalCarbonReportPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCarbonReportPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalCarbonReportPolicyPriorityOffsetAdjuster(
    var weight: Double,
) : GlobalCarbonReportPolicyModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class RoundingGlobalCarbonReportPolicyAdjuster(var step: Double) : GlobalCarbonReportPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double = roundTo(value, step)
}

data class NameGlobalCarbonReportPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCarbonReportPolicyModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalCarbonReportPolicyAdjuster(var minimum: Double) : GlobalCarbonReportPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCarbonReportPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global carbon report policy steps in order and keeps a trace of each. */
data class GlobalCarbonReportPolicyEngine(
    var steps: MutableList<GlobalCarbonReportPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalCarbonReportPolicy): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCarbonReportPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalCarbonReportPolicyEngine(flags: Registry): GlobalCarbonReportPolicyEngine {
    return GlobalCarbonReportPolicyEngine(
        steps = mutableListOf<GlobalCarbonReportPolicyModifier>(
            PercentGlobalCarbonReportPolicyAdjuster(5.7),
            FlaggedGlobalCarbonReportPolicyAdjuster(flags, "global-carbon-report-policy-scheduled", -2.9),
            GlobalCarbonReportPolicyPriorityOffsetAdjuster(1.96),
            RoundingGlobalCarbonReportPolicyAdjuster(0.25),
            NameGlobalCarbonReportPolicyAdjuster(
                mutableMapOf<String, Double>("north" to 1.11, "east" to 0.79, "central" to 1.28),
            ),
            MinimumGlobalCarbonReportPolicyAdjuster(49.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 81004.7,
    )
}

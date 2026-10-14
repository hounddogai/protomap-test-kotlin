package com.acme.transport.emissions.carbonreport.claim

/** One step of the default carbon report assignment calculation. */
interface DefaultCarbonReportAssignmentModifier {
    fun describe(): String
    fun adjust(item: DefaultCarbonReportAssignment, value: Double): Double
}

data class CapDefaultCarbonReportAssignmentAdjuster(var cap: Double) : DefaultCarbonReportAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultCarbonReportAssignment, value: Double): Double = minOf(value, cap)
}

data class CountryDefaultCarbonReportAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultCarbonReportAssignmentModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: DefaultCarbonReportAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultCarbonReportAssignmentAdjuster(
    var percent: Double,
) : DefaultCarbonReportAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCarbonReportAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default carbon report assignment steps in order and keeps a trace of each. */
data class DefaultCarbonReportAssignmentEngine(
    var steps: MutableList<DefaultCarbonReportAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultCarbonReportAssignment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCarbonReportAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCarbonReportAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultCarbonReportAssignmentEngine(): DefaultCarbonReportAssignmentEngine {
    return DefaultCarbonReportAssignmentEngine(
        steps = mutableListOf<DefaultCarbonReportAssignmentModifier>(
            CapDefaultCarbonReportAssignmentAdjuster(23272.0),
            CountryDefaultCarbonReportAssignmentAdjuster(mutableMapOf<String, Double>("west" to 1.29)),
            PercentDefaultCarbonReportAssignmentAdjuster(-5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 46184.4,
    )
}

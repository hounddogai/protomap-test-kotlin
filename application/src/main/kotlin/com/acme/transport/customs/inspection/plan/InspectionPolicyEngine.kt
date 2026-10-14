package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.Registry

/** One step of the inspection policy calculation. */
interface InspectionPolicyModifier {
    fun describe(): String
    fun adjust(item: InspectionPolicy, value: Double): Double
}

data class FlaggedInspectionPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneInspectionPolicyAdjuster(var factors: MutableMap<String, Double>) : InspectionPolicyModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: InspectionPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumInspectionPolicyAdjuster(var minimum: Double) : InspectionPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection policy steps in order and keeps a trace of each. */
data class InspectionPolicyEngine(
    var steps: MutableList<InspectionPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionPolicyEngine(flags: Registry): InspectionPolicyEngine {
    return InspectionPolicyEngine(
        steps = mutableListOf<InspectionPolicyModifier>(
            FlaggedInspectionPolicyAdjuster(flags, "inspection-policy-batched", 9.2),
            LaneInspectionPolicyAdjuster(mutableMapOf<String, Double>("online" to 1.08, "retail" to 0.92)),
            MinimumInspectionPolicyAdjuster(15.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 91002.0,
    )
}

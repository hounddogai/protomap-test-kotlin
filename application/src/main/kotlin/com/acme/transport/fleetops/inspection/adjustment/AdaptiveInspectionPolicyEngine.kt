package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive inspection policy calculation. */
interface AdaptiveInspectionPolicyModifier {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionPolicy, value: Double): Double
}

data class RoundingAdaptiveInspectionPolicyAdjuster(var step: Double) : AdaptiveInspectionPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveInspectionPolicy, value: Double): Double = roundTo(value, step)
}

data class TieredAdaptiveInspectionPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveInspectionPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveInspectionPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveInspectionPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneAdaptiveInspectionPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionPolicyModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: AdaptiveInspectionPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive inspection policy steps in order and keeps a trace of each. */
data class AdaptiveInspectionPolicyEngine(
    var steps: MutableList<AdaptiveInspectionPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveInspectionPolicy): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionPolicyEngine(flags: Registry): AdaptiveInspectionPolicyEngine {
    return AdaptiveInspectionPolicyEngine(
        steps = mutableListOf<AdaptiveInspectionPolicyModifier>(
            RoundingAdaptiveInspectionPolicyAdjuster(0.1),
            TieredAdaptiveInspectionPolicyAdjuster(
                mutableListOf<Double>(7504.0, 8199.0),
                mutableListOf<Double>(0.98, 1.03),
            ),
            FlaggedAdaptiveInspectionPolicyAdjuster(flags, "adaptive-inspection-policy-default", 7.3),
            LaneAdaptiveInspectionPolicyAdjuster(
                mutableMapOf<String, Double>("east" to 1.11, "south" to 1.02, "west" to 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 80473.1,
    )
}

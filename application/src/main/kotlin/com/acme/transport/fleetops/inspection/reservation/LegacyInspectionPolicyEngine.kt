package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy inspection policy calculation. */
interface LegacyInspectionPolicyModifier {
    fun describe(): String
    fun adjust(item: LegacyInspectionPolicy, value: Double): Double
}

data class TieredLegacyInspectionPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyInspectionPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyInspectionPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyInspectionPolicyAdjuster(var step: Double) : LegacyInspectionPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyInspectionPolicy, value: Double): Double = roundTo(value, step)
}

data class LegacyInspectionPolicyRetriesOffsetAdjuster(var weight: Double) : LegacyInspectionPolicyModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: LegacyInspectionPolicy, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class CategoryLegacyInspectionPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionPolicyModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: LegacyInspectionPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyInspectionPolicyAdjuster(var percent: Double) : LegacyInspectionPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyInspectionPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy inspection policy steps in order and keeps a trace of each. */
data class LegacyInspectionPolicyEngine(
    var steps: MutableList<LegacyInspectionPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyInspectionPolicy): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyInspectionPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyInspectionPolicyEngine(): LegacyInspectionPolicyEngine {
    return LegacyInspectionPolicyEngine(
        steps = mutableListOf<LegacyInspectionPolicyModifier>(
            TieredLegacyInspectionPolicyAdjuster(
                mutableListOf<Double>(1480.0, 1631.0),
                mutableListOf<Double>(1.07, 1.11),
            ),
            RoundingLegacyInspectionPolicyAdjuster(0.05),
            LegacyInspectionPolicyRetriesOffsetAdjuster(0.11),
            CategoryLegacyInspectionPolicyAdjuster(
                mutableMapOf<String, Double>("north" to 0.73, "online" to 0.86),
            ),
            PercentLegacyInspectionPolicyAdjuster(10.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 63165.0,
    )
}

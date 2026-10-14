package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling clearance policy calculation. */
interface RollingClearancePolicyModifier {
    fun describe(): String
    fun adjust(item: RollingClearancePolicy, value: Double): Double
}

data class TieredRollingClearancePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingClearancePolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingClearancePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingClearancePolicyAdjuster(var minimum: Double) : RollingClearancePolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingClearancePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingClearancePolicyDurationMinutesOffsetAdjuster(
    var weight: Double,
) : RollingClearancePolicyModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RollingClearancePolicy, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class PercentRollingClearancePolicyAdjuster(var percent: Double) : RollingClearancePolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingClearancePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling clearance policy steps in order and keeps a trace of each. */
data class RollingClearancePolicyEngine(
    var steps: MutableList<RollingClearancePolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingClearancePolicy): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingClearancePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingClearancePolicyEngine(): RollingClearancePolicyEngine {
    return RollingClearancePolicyEngine(
        steps = mutableListOf<RollingClearancePolicyModifier>(
            TieredRollingClearancePolicyAdjuster(
                mutableListOf<Double>(1913.0, 7414.0),
                mutableListOf<Double>(1.14, 1.05),
            ),
            MinimumRollingClearancePolicyAdjuster(47.2),
            RollingClearancePolicyDurationMinutesOffsetAdjuster(1.59),
            PercentRollingClearancePolicyAdjuster(0.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 55754.8,
    )
}

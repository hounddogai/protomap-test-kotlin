package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped backhaul policy calculation. */
interface CappedBackhaulPolicyModifier {
    fun describe(): String
    fun adjust(item: CappedBackhaulPolicy, value: Double): Double
}

data class RoundingCappedBackhaulPolicyAdjuster(var step: Double) : CappedBackhaulPolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBackhaulPolicy, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedBackhaulPolicyAdjuster(var minimum: Double) : CappedBackhaulPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBackhaulPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedBackhaulPolicyBudgetOffsetAdjuster(var weight: Double) : CappedBackhaulPolicyModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CappedBackhaulPolicy, value: Double): Double = value + item.budget * weight
}

data class CapCappedBackhaulPolicyAdjuster(var cap: Double) : CappedBackhaulPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedBackhaulPolicy, value: Double): Double = minOf(value, cap)
}

data class PercentCappedBackhaulPolicyAdjuster(var percent: Double) : CappedBackhaulPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedBackhaulPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCappedBackhaulPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedBackhaulPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedBackhaulPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped backhaul policy steps in order and keeps a trace of each. */
data class CappedBackhaulPolicyEngine(
    var steps: MutableList<CappedBackhaulPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedBackhaulPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedBackhaulPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedBackhaulPolicyEngine(): CappedBackhaulPolicyEngine {
    return CappedBackhaulPolicyEngine(
        steps = mutableListOf<CappedBackhaulPolicyModifier>(
            RoundingCappedBackhaulPolicyAdjuster(0.1),
            MinimumCappedBackhaulPolicyAdjuster(42.4),
            CappedBackhaulPolicyBudgetOffsetAdjuster(1.26),
            CapCappedBackhaulPolicyAdjuster(47387.0),
            PercentCappedBackhaulPolicyAdjuster(23.6),
            TieredCappedBackhaulPolicyAdjuster(
                mutableListOf<Double>(7661.0, 8315.0),
                mutableListOf<Double>(0.87, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 62137.6,
    )
}

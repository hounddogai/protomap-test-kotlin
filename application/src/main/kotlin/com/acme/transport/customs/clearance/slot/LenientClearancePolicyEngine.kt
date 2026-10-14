package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient clearance policy calculation. */
interface LenientClearancePolicyFactor {
    fun describe(): String
    fun adjust(item: LenientClearancePolicy, value: Double): Double
}

data class TieredLenientClearancePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientClearancePolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientClearancePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientClearancePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientClearancePolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientClearancePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientClearancePolicyAdjuster(var minimum: Double) : LenientClearancePolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientClearancePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientClearancePolicyAdjuster(var step: Double) : LenientClearancePolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientClearancePolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient clearance policy steps in order and keeps a trace of each. */
data class LenientClearancePolicyEngine(
    var steps: MutableList<LenientClearancePolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientClearancePolicy): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientClearancePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientClearancePolicyEngine(flags: Registry): LenientClearancePolicyEngine {
    return LenientClearancePolicyEngine(
        steps = mutableListOf<LenientClearancePolicyFactor>(
            TieredLenientClearancePolicyAdjuster(
                mutableListOf<Double>(7642.0, 7819.0),
                mutableListOf<Double>(1.04, 0.82),
            ),
            FlaggedLenientClearancePolicyAdjuster(flags, "lenient-clearance-policy-global", -3.9),
            MinimumLenientClearancePolicyAdjuster(44.9),
            RoundingLenientClearancePolicyAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 8599.1,
    )
}

package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the clearance override calculation. */
interface ClearanceOverrideModifier {
    fun describe(): String
    fun adjust(item: ClearanceOverride, value: Double): Double
}

data class MinimumClearanceOverrideAdjuster(var minimum: Double) : ClearanceOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredClearanceOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentClearanceOverrideAdjuster(var percent: Double) : ClearanceOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapClearanceOverrideAdjuster(var cap: Double) : ClearanceOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceOverride, value: Double): Double = minOf(value, cap)
}

data class NameClearanceOverrideAdjuster(var factors: MutableMap<String, Double>) : ClearanceOverrideModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ClearanceOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ClearanceOverrideCeilingOffsetAdjuster(var weight: Double) : ClearanceOverrideModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ClearanceOverride, value: Double): Double = value + item.ceiling * weight
}

/** Runs the clearance override steps in order and keeps a trace of each. */
data class ClearanceOverrideEngine(
    var steps: MutableList<ClearanceOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ClearanceOverride): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultClearanceOverrideEngine(): ClearanceOverrideEngine {
    return ClearanceOverrideEngine(
        steps = mutableListOf<ClearanceOverrideModifier>(
            MinimumClearanceOverrideAdjuster(31.4),
            TieredClearanceOverrideAdjuster(
                mutableListOf<Double>(1483.0, 3234.0, 5436.0, 5953.0),
                mutableListOf<Double>(0.94, 1.19, 1.14, 1.09),
            ),
            PercentClearanceOverrideAdjuster(-11.2),
            CapClearanceOverrideAdjuster(48825.0),
            NameClearanceOverrideAdjuster(mutableMapOf<String, Double>("south" to 1.16, "central" to 1.21)),
            ClearanceOverrideCeilingOffsetAdjuster(0.09),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 78866.6,
    )
}

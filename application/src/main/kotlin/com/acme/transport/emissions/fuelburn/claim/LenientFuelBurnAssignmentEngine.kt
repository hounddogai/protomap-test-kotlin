package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient fuel burn assignment calculation. */
interface LenientFuelBurnAssignmentModifier {
    fun describe(): String
    fun adjust(item: LenientFuelBurnAssignment, value: Double): Double
}

data class TieredLenientFuelBurnAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFuelBurnAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFuelBurnAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientFuelBurnAssignmentAdjuster(var percent: Double) : LenientFuelBurnAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientFuelBurnAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientFuelBurnAssignmentAdjuster(var minimum: Double) : LenientFuelBurnAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientFuelBurnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLenientFuelBurnAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientFuelBurnAssignmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientFuelBurnAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientFuelBurnAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFuelBurnAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFuelBurnAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient fuel burn assignment steps in order and keeps a trace of each. */
data class LenientFuelBurnAssignmentEngine(
    var steps: MutableList<LenientFuelBurnAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientFuelBurnAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientFuelBurnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientFuelBurnAssignmentEngine(flags: Registry): LenientFuelBurnAssignmentEngine {
    return LenientFuelBurnAssignmentEngine(
        steps = mutableListOf<LenientFuelBurnAssignmentModifier>(
            TieredLenientFuelBurnAssignmentAdjuster(
                mutableListOf<Double>(4569.0, 4783.0),
                mutableListOf<Double>(1.16, 0.97),
            ),
            PercentLenientFuelBurnAssignmentAdjuster(14.7),
            MinimumLenientFuelBurnAssignmentAdjuster(47.9),
            TitleLenientFuelBurnAssignmentAdjuster(mutableMapOf<String, Double>("online" to 1.13)),
            FlaggedLenientFuelBurnAssignmentAdjuster(flags, "lenient-fuel-burn-assignment-seasonal", -7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 8486.5,
    )
}

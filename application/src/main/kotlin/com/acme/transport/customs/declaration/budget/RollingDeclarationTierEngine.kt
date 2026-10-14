package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling declaration tier calculation. */
interface RollingDeclarationTierAdjuster {
    fun describe(): String
    fun adjust(item: RollingDeclarationTier, value: Double): Double
}

data class PercentRollingDeclarationTierAdjuster(var percent: Double) : RollingDeclarationTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDeclarationTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingDeclarationTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDeclarationTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDeclarationTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRollingDeclarationTierAdjuster(var minimum: Double) : RollingDeclarationTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDeclarationTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingDeclarationTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDeclarationTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDeclarationTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingDeclarationTierAdjuster(var step: Double) : RollingDeclarationTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDeclarationTier, value: Double): Double = roundTo(value, step)
}

data class RollingDeclarationTierBudgetOffsetAdjuster(var weight: Double) : RollingDeclarationTierAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RollingDeclarationTier, value: Double): Double = value + item.budget * weight
}

/** Runs the rolling declaration tier steps in order and keeps a trace of each. */
data class RollingDeclarationTierEngine(
    var steps: MutableList<RollingDeclarationTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDeclarationTier): Double {
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

    fun evaluateAll(items: MutableList<RollingDeclarationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDeclarationTierEngine(flags: Registry): RollingDeclarationTierEngine {
    return RollingDeclarationTierEngine(
        steps = mutableListOf<RollingDeclarationTierAdjuster>(
            PercentRollingDeclarationTierAdjuster(-23.7),
            FlaggedRollingDeclarationTierAdjuster(flags, "rolling-declaration-tier-primary", -10.0),
            MinimumRollingDeclarationTierAdjuster(34.2),
            TieredRollingDeclarationTierAdjuster(
                mutableListOf<Double>(5874.0, 7675.0),
                mutableListOf<Double>(0.81, 0.86),
            ),
            RoundingRollingDeclarationTierAdjuster(0.25),
            RollingDeclarationTierBudgetOffsetAdjuster(0.81),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 39139.0,
    )
}

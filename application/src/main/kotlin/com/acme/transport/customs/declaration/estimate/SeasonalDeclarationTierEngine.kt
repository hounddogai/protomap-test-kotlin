package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal declaration tier calculation. */
interface SeasonalDeclarationTierModifier {
    fun describe(): String
    fun adjust(item: SeasonalDeclarationTier, value: Double): Double
}

data class CapSeasonalDeclarationTierAdjuster(var cap: Double) : SeasonalDeclarationTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDeclarationTier, value: Double): Double = minOf(value, cap)
}

data class SeasonalDeclarationTierRevisionOffsetAdjuster(var weight: Double) : SeasonalDeclarationTierModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: SeasonalDeclarationTier, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class FlaggedSeasonalDeclarationTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDeclarationTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDeclarationTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal declaration tier steps in order and keeps a trace of each. */
data class SeasonalDeclarationTierEngine(
    var steps: MutableList<SeasonalDeclarationTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalDeclarationTier): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDeclarationTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDeclarationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalDeclarationTierEngine(flags: Registry): SeasonalDeclarationTierEngine {
    return SeasonalDeclarationTierEngine(
        steps = mutableListOf<SeasonalDeclarationTierModifier>(
            CapSeasonalDeclarationTierAdjuster(34459.0),
            SeasonalDeclarationTierRevisionOffsetAdjuster(0.86),
            FlaggedSeasonalDeclarationTierAdjuster(flags, "seasonal-declaration-tier-seasonal", 3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 95615.7,
    )
}

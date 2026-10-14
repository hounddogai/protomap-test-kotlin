package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.flags.Registry

/** One step of the tiered declaration baseline calculation. */
interface TieredDeclarationBaselineModifier {
    fun describe(): String
    fun adjust(item: TieredDeclarationBaseline, value: Double): Double
}

data class FlaggedTieredDeclarationBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDeclarationBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDeclarationBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDeclarationBaselineGraceDaysOffsetAdjuster(
    var weight: Double,
) : TieredDeclarationBaselineModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TieredDeclarationBaseline, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class MinimumTieredDeclarationBaselineAdjuster(var minimum: Double) : TieredDeclarationBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDeclarationBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelTieredDeclarationBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDeclarationBaselineModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredDeclarationBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered declaration baseline steps in order and keeps a trace of each. */
data class TieredDeclarationBaselineEngine(
    var steps: MutableList<TieredDeclarationBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredDeclarationBaseline): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDeclarationBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDeclarationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredDeclarationBaselineEngine(flags: Registry): TieredDeclarationBaselineEngine {
    return TieredDeclarationBaselineEngine(
        steps = mutableListOf<TieredDeclarationBaselineModifier>(
            FlaggedTieredDeclarationBaselineAdjuster(flags, "tiered-declaration-baseline-regional", -2.8),
            TieredDeclarationBaselineGraceDaysOffsetAdjuster(0.48),
            MinimumTieredDeclarationBaselineAdjuster(17.1),
            LabelTieredDeclarationBaselineAdjuster(
                mutableMapOf<String, Double>("south" to 1.21, "retail" to 0.96, "central" to 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 19866.0,
    )
}

package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.flags.Registry

/** One step of the declaration override calculation. */
interface DeclarationOverrideAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationOverride, value: Double): Double
}

data class NameDeclarationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationOverrideAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: DeclarationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDeclarationOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DeclarationOverrideAmountOffsetAdjuster(var weight: Double) : DeclarationOverrideAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: DeclarationOverride, value: Double): Double = value + item.amount * weight
}

data class TieredDeclarationOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the declaration override steps in order and keeps a trace of each. */
data class DeclarationOverrideEngine(
    var steps: MutableList<DeclarationOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DeclarationOverride): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDeclarationOverrideEngine(flags: Registry): DeclarationOverrideEngine {
    return DeclarationOverrideEngine(
        steps = mutableListOf<DeclarationOverrideAdjuster>(
            NameDeclarationOverrideAdjuster(
                mutableMapOf<String, Double>("north" to 0.84, "retail" to 0.93, "south" to 0.97),
            ),
            FlaggedDeclarationOverrideAdjuster(flags, "declaration-override-tiered", 3.3),
            DeclarationOverrideAmountOffsetAdjuster(1.18),
            TieredDeclarationOverrideAdjuster(
                mutableListOf<Double>(488.0, 1918.0, 2973.0, 3316.0),
                mutableListOf<Double>(0.95, 1.07, 0.99, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 77381.8,
    )
}

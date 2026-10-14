package com.acme.transport.customs.declaration.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration manifest calculation. */
interface DeclarationManifestModifier {
    fun describe(): String
    fun adjust(item: DeclarationManifest, value: Double): Double
}

data class RoundingDeclarationManifestAdjuster(var step: Double) : DeclarationManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationManifest, value: Double): Double = roundTo(value, step)
}

data class TitleDeclarationManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : DeclarationManifestModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: DeclarationManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDeclarationManifestAdjuster(var cap: Double) : DeclarationManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationManifest, value: Double): Double = minOf(value, cap)
}

data class FlaggedDeclarationManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDeclarationManifestAdjuster(var percent: Double) : DeclarationManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDeclarationManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the declaration manifest steps in order and keeps a trace of each. */
data class DeclarationManifestEngine(
    var steps: MutableList<DeclarationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DeclarationManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDeclarationManifestEngine(flags: Registry): DeclarationManifestEngine {
    return DeclarationManifestEngine(
        steps = mutableListOf<DeclarationManifestModifier>(
            RoundingDeclarationManifestAdjuster(0.25),
            TitleDeclarationManifestAdjuster(mutableMapOf<String, Double>("north" to 1.32, "east" to 1.36)),
            CapDeclarationManifestAdjuster(7747.0),
            FlaggedDeclarationManifestAdjuster(flags, "declaration-manifest-scheduled", -4.5),
            PercentDeclarationManifestAdjuster(-3.0),
            TieredDeclarationManifestAdjuster(
                mutableListOf<Double>(7408.0, 7420.0, 9228.0, 9335.0),
                mutableListOf<Double>(1.18, 1.09, 0.84, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 64778.8,
    )
}

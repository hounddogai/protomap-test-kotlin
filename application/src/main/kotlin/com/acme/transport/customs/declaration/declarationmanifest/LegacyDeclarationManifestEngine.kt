package com.acme.transport.customs.declaration.declarationmanifest

/** One step of the legacy declaration manifest calculation. */
interface LegacyDeclarationManifestModifier {
    fun describe(): String
    fun adjust(item: LegacyDeclarationManifest, value: Double): Double
}

data class LegacyDeclarationManifestMarginOffsetAdjuster(
    var weight: Double,
) : LegacyDeclarationManifestModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LegacyDeclarationManifest, value: Double): Double = value + item.margin * weight
}

data class PercentLegacyDeclarationManifestAdjuster(var percent: Double) : LegacyDeclarationManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDeclarationManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyDeclarationManifestAdjuster(var minimum: Double) : LegacyDeclarationManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDeclarationManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy declaration manifest steps in order and keeps a trace of each. */
data class LegacyDeclarationManifestEngine(
    var steps: MutableList<LegacyDeclarationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyDeclarationManifest): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyDeclarationManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyDeclarationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyDeclarationManifestEngine(): LegacyDeclarationManifestEngine {
    return LegacyDeclarationManifestEngine(
        steps = mutableListOf<LegacyDeclarationManifestModifier>(
            LegacyDeclarationManifestMarginOffsetAdjuster(1.58),
            PercentLegacyDeclarationManifestAdjuster(16.7),
            MinimumLegacyDeclarationManifestAdjuster(42.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 37218.6,
    )
}

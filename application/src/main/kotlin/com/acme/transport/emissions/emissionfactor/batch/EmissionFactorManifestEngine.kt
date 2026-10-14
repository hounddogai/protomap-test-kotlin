package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor manifest calculation. */
interface EmissionFactorManifestModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorManifest, value: Double): Double
}

data class RoundingEmissionFactorManifestAdjuster(var step: Double) : EmissionFactorManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorManifest, value: Double): Double = roundTo(value, step)
}

data class CapEmissionFactorManifestAdjuster(var cap: Double) : EmissionFactorManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumEmissionFactorManifestAdjuster(var minimum: Double) : EmissionFactorManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class EmissionFactorManifestCreatedAtOffsetAdjuster(var weight: Double) : EmissionFactorManifestModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: EmissionFactorManifest, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class PercentEmissionFactorManifestAdjuster(var percent: Double) : EmissionFactorManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the emission factor manifest steps in order and keeps a trace of each. */
data class EmissionFactorManifestEngine(
    var steps: MutableList<EmissionFactorManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorManifest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorManifestEngine(): EmissionFactorManifestEngine {
    return EmissionFactorManifestEngine(
        steps = mutableListOf<EmissionFactorManifestModifier>(
            RoundingEmissionFactorManifestAdjuster(0.1),
            CapEmissionFactorManifestAdjuster(17451.0),
            MinimumEmissionFactorManifestAdjuster(1.5),
            EmissionFactorManifestCreatedAtOffsetAdjuster(1.11),
            PercentEmissionFactorManifestAdjuster(-22.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 56567.4,
    )
}

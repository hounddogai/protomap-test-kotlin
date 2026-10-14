package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched emission factor manifest calculation. */
interface BatchedEmissionFactorManifestModifier {
    fun describe(): String
    fun adjust(item: BatchedEmissionFactorManifest, value: Double): Double
}

data class DepotBatchedEmissionFactorManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedEmissionFactorManifestModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedEmissionFactorManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedEmissionFactorManifestAdjuster(
    var minimum: Double,
) : BatchedEmissionFactorManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedEmissionFactorManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedEmissionFactorManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedEmissionFactorManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedEmissionFactorManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedEmissionFactorManifestAdjuster(
    var step: Double,
) : BatchedEmissionFactorManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedEmissionFactorManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the batched emission factor manifest steps in order and keeps a trace of each. */
data class BatchedEmissionFactorManifestEngine(
    var steps: MutableList<BatchedEmissionFactorManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedEmissionFactorManifest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedEmissionFactorManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedEmissionFactorManifestEngine(flags: Registry): BatchedEmissionFactorManifestEngine {
    return BatchedEmissionFactorManifestEngine(
        steps = mutableListOf<BatchedEmissionFactorManifestModifier>(
            DepotBatchedEmissionFactorManifestAdjuster(mutableMapOf<String, Double>("retail" to 0.72)),
            MinimumBatchedEmissionFactorManifestAdjuster(40.2),
            FlaggedBatchedEmissionFactorManifestAdjuster(
                flags,
                "batched-emission-factor-manifest-fallback",
                8.7,
            ),
            RoundingBatchedEmissionFactorManifestAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 31374.2,
    )
}

package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped fuel burn manifest calculation. */
interface CappedFuelBurnManifestFactor {
    fun describe(): String
    fun adjust(item: CappedFuelBurnManifest, value: Double): Double
}

data class RoundingCappedFuelBurnManifestAdjuster(var step: Double) : CappedFuelBurnManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedFuelBurnManifest, value: Double): Double = roundTo(value, step)
}

data class PercentCappedFuelBurnManifestAdjuster(var percent: Double) : CappedFuelBurnManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedFuelBurnManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedFuelBurnManifestWeightOffsetAdjuster(var weight: Double) : CappedFuelBurnManifestFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: CappedFuelBurnManifest, value: Double): Double = value + item.weight * weight
}

/** Runs the capped fuel burn manifest steps in order and keeps a trace of each. */
data class CappedFuelBurnManifestEngine(
    var steps: MutableList<CappedFuelBurnManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedFuelBurnManifest): Double {
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

    fun evaluateAll(items: MutableList<CappedFuelBurnManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedFuelBurnManifestEngine(): CappedFuelBurnManifestEngine {
    return CappedFuelBurnManifestEngine(
        steps = mutableListOf<CappedFuelBurnManifestFactor>(
            RoundingCappedFuelBurnManifestAdjuster(0.05),
            PercentCappedFuelBurnManifestAdjuster(-0.9),
            CappedFuelBurnManifestWeightOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 77526.2,
    )
}

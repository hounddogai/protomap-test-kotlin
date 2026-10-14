package com.acme.transport.customs.clearance.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the clearance manifest calculation. */
interface ClearanceManifestModifier {
    fun describe(): String
    fun adjust(item: ClearanceManifest, value: Double): Double
}

data class CapClearanceManifestAdjuster(var cap: Double) : ClearanceManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceManifest, value: Double): Double = minOf(value, cap)
}

data class TieredClearanceManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameClearanceManifestAdjuster(var factors: MutableMap<String, Double>) : ClearanceManifestModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ClearanceManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the clearance manifest steps in order and keeps a trace of each. */
data class ClearanceManifestEngine(
    var steps: MutableList<ClearanceManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceManifest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceManifestEngine(): ClearanceManifestEngine {
    return ClearanceManifestEngine(
        steps = mutableListOf<ClearanceManifestModifier>(
            CapClearanceManifestAdjuster(22410.0),
            TieredClearanceManifestAdjuster(
                mutableListOf<Double>(6456.0, 7404.0, 7632.0),
                mutableListOf<Double>(1.19, 1.01, 0.9),
            ),
            NameClearanceManifestAdjuster(mutableMapOf<String, Double>("central" to 1.08)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 12883.6,
    )
}

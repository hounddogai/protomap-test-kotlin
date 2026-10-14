package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the violation manifest calculation. */
interface ViolationManifestModifier {
    fun describe(): String
    fun adjust(item: ViolationManifest, value: Double): Double
}

data class CapViolationManifestAdjuster(var cap: Double) : ViolationManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationManifest, value: Double): Double = minOf(value, cap)
}

data class TieredViolationManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentViolationManifestAdjuster(var percent: Double) : ViolationManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ViolationManifestAttemptsOffsetAdjuster(var weight: Double) : ViolationManifestModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ViolationManifest, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the violation manifest steps in order and keeps a trace of each. */
data class ViolationManifestEngine(
    var steps: MutableList<ViolationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationManifest): Double {
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

    fun evaluateAll(items: MutableList<ViolationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationManifestEngine(): ViolationManifestEngine {
    return ViolationManifestEngine(
        steps = mutableListOf<ViolationManifestModifier>(
            CapViolationManifestAdjuster(46323.0),
            TieredViolationManifestAdjuster(
                mutableListOf<Double>(1876.0, 5756.0, 9382.0),
                mutableListOf<Double>(1.08, 1.09, 0.85),
            ),
            PercentViolationManifestAdjuster(15.2),
            ViolationManifestAttemptsOffsetAdjuster(0.21),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 70840.5,
    )
}

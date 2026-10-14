package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss manifest calculation. */
interface NearMissManifestAdjuster {
    fun describe(): String
    fun adjust(item: NearMissManifest, value: Double): Double
}

data class MinimumNearMissManifestAdjuster(var minimum: Double) : NearMissManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingNearMissManifestAdjuster(var step: Double) : NearMissManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissManifest, value: Double): Double = roundTo(value, step)
}

data class CapNearMissManifestAdjuster(var cap: Double) : NearMissManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissManifest, value: Double): Double = minOf(value, cap)
}

data class PercentNearMissManifestAdjuster(var percent: Double) : NearMissManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the near miss manifest steps in order and keeps a trace of each. */
data class NearMissManifestEngine(
    var steps: MutableList<NearMissManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NearMissManifest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNearMissManifestEngine(): NearMissManifestEngine {
    return NearMissManifestEngine(
        steps = mutableListOf<NearMissManifestAdjuster>(
            MinimumNearMissManifestAdjuster(20.0),
            RoundingNearMissManifestAdjuster(5.0),
            CapNearMissManifestAdjuster(38844.0),
            PercentNearMissManifestAdjuster(16.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 46830.9,
    )
}

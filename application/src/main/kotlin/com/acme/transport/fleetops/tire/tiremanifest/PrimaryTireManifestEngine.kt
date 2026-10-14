package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary tire manifest calculation. */
interface PrimaryTireManifestAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryTireManifest, value: Double): Double
}

data class CapPrimaryTireManifestAdjuster(var cap: Double) : PrimaryTireManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTireManifest, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryTireManifestAdjuster(var minimum: Double) : PrimaryTireManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryTireManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelPrimaryTireManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryTireManifestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryTireManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryTireManifestAdjuster(var percent: Double) : PrimaryTireManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTireManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryTireManifestAdjuster(var step: Double) : PrimaryTireManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTireManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the primary tire manifest steps in order and keeps a trace of each. */
data class PrimaryTireManifestEngine(
    var steps: MutableList<PrimaryTireManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryTireManifest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryTireManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryTireManifestEngine(): PrimaryTireManifestEngine {
    return PrimaryTireManifestEngine(
        steps = mutableListOf<PrimaryTireManifestAdjuster>(
            CapPrimaryTireManifestAdjuster(45742.0),
            MinimumPrimaryTireManifestAdjuster(26.2),
            LabelPrimaryTireManifestAdjuster(
                mutableMapOf<String, Double>("west" to 0.83, "north" to 0.81, "online" to 1.03),
            ),
            PercentPrimaryTireManifestAdjuster(-15.1),
            RoundingPrimaryTireManifestAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 93023.9,
    )
}

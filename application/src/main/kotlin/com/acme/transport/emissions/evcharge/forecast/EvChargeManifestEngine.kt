package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge manifest calculation. */
interface EvChargeManifestAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeManifest, value: Double): Double
}

data class SegmentEvChargeManifestAdjuster(var factors: MutableMap<String, Double>) : EvChargeManifestAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: EvChargeManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEvChargeManifestAdjuster(var step: Double) : EvChargeManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeManifest, value: Double): Double = roundTo(value, step)
}

data class CapEvChargeManifestAdjuster(var cap: Double) : EvChargeManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the ev charge manifest steps in order and keeps a trace of each. */
data class EvChargeManifestEngine(
    var steps: MutableList<EvChargeManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeManifest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeManifestEngine(): EvChargeManifestEngine {
    return EvChargeManifestEngine(
        steps = mutableListOf<EvChargeManifestAdjuster>(
            SegmentEvChargeManifestAdjuster(mutableMapOf<String, Double>("retail" to 1.34, "online" to 1.15)),
            RoundingEvChargeManifestAdjuster(5.0),
            CapEvChargeManifestAdjuster(33237.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 9427.1,
    )
}

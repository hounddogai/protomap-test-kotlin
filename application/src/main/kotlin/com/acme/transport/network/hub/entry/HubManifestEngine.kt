package com.acme.transport.network.hub.entry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub manifest calculation. */
interface HubManifestFactor {
    fun describe(): String
    fun adjust(item: HubManifest, value: Double): Double
}

data class MinimumHubManifestAdjuster(var minimum: Double) : HubManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class HubManifestEndsAtOffsetAdjuster(var weight: Double) : HubManifestFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: HubManifest, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class SegmentHubManifestAdjuster(var factors: MutableMap<String, Double>) : HubManifestFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: HubManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentHubManifestAdjuster(var percent: Double) : HubManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingHubManifestAdjuster(var step: Double) : HubManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the hub manifest steps in order and keeps a trace of each. */
data class HubManifestEngine(
    var steps: MutableList<HubManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubManifestEngine(): HubManifestEngine {
    return HubManifestEngine(
        steps = mutableListOf<HubManifestFactor>(
            MinimumHubManifestAdjuster(22.2),
            HubManifestEndsAtOffsetAdjuster(0.35),
            SegmentHubManifestAdjuster(
                mutableMapOf<String, Double>("east" to 1.12, "west" to 1.39, "north" to 1.06),
            ),
            PercentHubManifestAdjuster(15.0),
            RoundingHubManifestAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 24207.0,
    )
}

package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard digest calculation. */
interface TrailerYardDigestFactor {
    fun describe(): String
    fun adjust(item: TrailerYardDigest, value: Double): Double
}

data class TrailerYardDigestThresholdOffsetAdjuster(var weight: Double) : TrailerYardDigestFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: TrailerYardDigest, value: Double): Double = value + item.threshold * weight
}

data class PercentTrailerYardDigestAdjuster(var percent: Double) : TrailerYardDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTrailerYardDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelTrailerYardDigestAdjuster(var factors: MutableMap<String, Double>) : TrailerYardDigestFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerYardDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer yard digest steps in order and keeps a trace of each. */
data class TrailerYardDigestEngine(
    var steps: MutableList<TrailerYardDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerYardDigest): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerYardDigestEngine(flags: Registry): TrailerYardDigestEngine {
    return TrailerYardDigestEngine(
        steps = mutableListOf<TrailerYardDigestFactor>(
            TrailerYardDigestThresholdOffsetAdjuster(0.2),
            PercentTrailerYardDigestAdjuster(-11.6),
            FlaggedTrailerYardDigestAdjuster(flags, "trailer-yard-digest-tiered", 6.6),
            LabelTrailerYardDigestAdjuster(mutableMapOf<String, Double>("north" to 1.31, "central" to 1.01)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 90020.4,
    )
}

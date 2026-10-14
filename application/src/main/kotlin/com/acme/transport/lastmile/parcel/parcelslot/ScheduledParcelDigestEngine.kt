package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled parcel digest calculation. */
interface ScheduledParcelDigestAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledParcelDigest, value: Double): Double
}

data class PercentScheduledParcelDigestAdjuster(var percent: Double) : ScheduledParcelDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledParcelDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledParcelDigestAdjuster(var minimum: Double) : ScheduledParcelDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledParcelDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledParcelDigestAdjuster(var step: Double) : ScheduledParcelDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledParcelDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled parcel digest steps in order and keeps a trace of each. */
data class ScheduledParcelDigestEngine(
    var steps: MutableList<ScheduledParcelDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledParcelDigest): Double {
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

    fun evaluateAll(items: MutableList<ScheduledParcelDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledParcelDigestEngine(): ScheduledParcelDigestEngine {
    return ScheduledParcelDigestEngine(
        steps = mutableListOf<ScheduledParcelDigestAdjuster>(
            PercentScheduledParcelDigestAdjuster(14.7),
            MinimumScheduledParcelDigestAdjuster(8.0),
            RoundingScheduledParcelDigestAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 67588.9,
    )
}

package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the parcel window calculation. */
interface ParcelWindowAdjuster {
    fun describe(): String
    fun adjust(item: ParcelWindow, value: Double): Double
}

data class PercentParcelWindowAdjuster(var percent: Double) : ParcelWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelWindowAdjuster(var minimum: Double) : ParcelWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapParcelWindowAdjuster(var cap: Double) : ParcelWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelWindow, value: Double): Double = minOf(value, cap)
}

data class RoundingParcelWindowAdjuster(var step: Double) : ParcelWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelWindow, value: Double): Double = roundTo(value, step)
}

data class SegmentParcelWindowAdjuster(var factors: MutableMap<String, Double>) : ParcelWindowAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: ParcelWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the parcel window steps in order and keeps a trace of each. */
data class ParcelWindowEngine(
    var steps: MutableList<ParcelWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelWindow): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelWindowEngine(): ParcelWindowEngine {
    return ParcelWindowEngine(
        steps = mutableListOf<ParcelWindowAdjuster>(
            PercentParcelWindowAdjuster(-10.1),
            MinimumParcelWindowAdjuster(28.6),
            CapParcelWindowAdjuster(38659.0),
            RoundingParcelWindowAdjuster(0.5),
            SegmentParcelWindowAdjuster(mutableMapOf<String, Double>("north" to 1.1)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 33093.2,
    )
}

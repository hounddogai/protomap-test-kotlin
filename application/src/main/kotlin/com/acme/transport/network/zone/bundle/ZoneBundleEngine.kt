package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone bundle calculation. */
interface ZoneBundleModifier {
    fun describe(): String
    fun adjust(item: ZoneBundle, value: Double): Double
}

data class LaneZoneBundleAdjuster(var factors: MutableMap<String, Double>) : ZoneBundleModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ZoneBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ZoneBundleMinimumOffsetAdjuster(var weight: Double) : ZoneBundleModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ZoneBundle, value: Double): Double = value + item.minimum * weight
}

data class CapZoneBundleAdjuster(var cap: Double) : ZoneBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingZoneBundleAdjuster(var step: Double) : ZoneBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneBundle, value: Double): Double = roundTo(value, step)
}

data class TieredZoneBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the zone bundle steps in order and keeps a trace of each. */
data class ZoneBundleEngine(
    var steps: MutableList<ZoneBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneBundle): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneBundleEngine(): ZoneBundleEngine {
    return ZoneBundleEngine(
        steps = mutableListOf<ZoneBundleModifier>(
            LaneZoneBundleAdjuster(
                mutableMapOf<String, Double>("online" to 0.89, "retail" to 1.02, "west" to 0.8),
            ),
            ZoneBundleMinimumOffsetAdjuster(1.17),
            CapZoneBundleAdjuster(7623.0),
            RoundingZoneBundleAdjuster(0.1),
            TieredZoneBundleAdjuster(
                mutableListOf<Double>(2040.0, 2238.0, 7132.0),
                mutableListOf<Double>(1.04, 1.1, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 88334.9,
    )
}

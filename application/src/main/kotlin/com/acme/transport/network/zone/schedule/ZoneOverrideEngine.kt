package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone override calculation. */
interface ZoneOverrideAdjuster {
    fun describe(): String
    fun adjust(item: ZoneOverride, value: Double): Double
}

data class RoundingZoneOverrideAdjuster(var step: Double) : ZoneOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneOverride, value: Double): Double = roundTo(value, step)
}

data class TieredZoneOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapZoneOverrideAdjuster(var cap: Double) : ZoneOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneOverride, value: Double): Double = minOf(value, cap)
}

data class LabelZoneOverrideAdjuster(var factors: MutableMap<String, Double>) : ZoneOverrideAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ZoneOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the zone override steps in order and keeps a trace of each. */
data class ZoneOverrideEngine(
    var steps: MutableList<ZoneOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ZoneOverride): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultZoneOverrideEngine(): ZoneOverrideEngine {
    return ZoneOverrideEngine(
        steps = mutableListOf<ZoneOverrideAdjuster>(
            RoundingZoneOverrideAdjuster(0.05),
            TieredZoneOverrideAdjuster(
                mutableListOf<Double>(1168.0, 3224.0, 3320.0, 5772.0),
                mutableListOf<Double>(0.99, 1.1, 1.04, 0.96),
            ),
            CapZoneOverrideAdjuster(46239.0),
            LabelZoneOverrideAdjuster(mutableMapOf<String, Double>("west" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 26273.0,
    )
}

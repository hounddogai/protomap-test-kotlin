package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cross dock entry calculation. */
interface CrossDockEntryModifier {
    fun describe(): String
    fun adjust(item: CrossDockEntry, value: Double): Double
}

data class MinimumCrossDockEntryAdjuster(var minimum: Double) : CrossDockEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCrossDockEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CrossDockEntryPalletsOffsetAdjuster(var weight: Double) : CrossDockEntryModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CrossDockEntry, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TitleCrossDockEntryAdjuster(var factors: MutableMap<String, Double>) : CrossDockEntryModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CrossDockEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCrossDockEntryAdjuster(var percent: Double) : CrossDockEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCrossDockEntryAdjuster(var cap: Double) : CrossDockEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the cross dock entry steps in order and keeps a trace of each. */
data class CrossDockEntryEngine(
    var steps: MutableList<CrossDockEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockEntry): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockEntryEngine(): CrossDockEntryEngine {
    return CrossDockEntryEngine(
        steps = mutableListOf<CrossDockEntryModifier>(
            MinimumCrossDockEntryAdjuster(30.9),
            TieredCrossDockEntryAdjuster(
                mutableListOf<Double>(1582.0, 3225.0, 6793.0, 7861.0),
                mutableListOf<Double>(1.11, 1.02, 0.81, 1.17),
            ),
            CrossDockEntryPalletsOffsetAdjuster(0.23),
            TitleCrossDockEntryAdjuster(mutableMapOf<String, Double>("east" to 1.12)),
            PercentCrossDockEntryAdjuster(19.4),
            CapCrossDockEntryAdjuster(39550.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 34500.5,
    )
}

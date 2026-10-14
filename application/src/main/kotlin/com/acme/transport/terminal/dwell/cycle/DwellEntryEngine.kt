package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dwell entry calculation. */
interface DwellEntryModifier {
    fun describe(): String
    fun adjust(item: DwellEntry, value: Double): Double
}

data class TieredDwellEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDwellEntryAdjuster(var minimum: Double) : DwellEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDwellEntryAdjuster(var percent: Double) : DwellEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneDwellEntryAdjuster(var factors: MutableMap<String, Double>) : DwellEntryModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: DwellEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dwell entry steps in order and keeps a trace of each. */
data class DwellEntryEngine(
    var steps: MutableList<DwellEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DwellEntry): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDwellEntryEngine(): DwellEntryEngine {
    return DwellEntryEngine(
        steps = mutableListOf<DwellEntryModifier>(
            TieredDwellEntryAdjuster(
                mutableListOf<Double>(88.0, 1953.0, 5521.0, 8162.0),
                mutableListOf<Double>(1.01, 0.8, 1.19, 0.91),
            ),
            MinimumDwellEntryAdjuster(41.8),
            PercentDwellEntryAdjuster(15.8),
            LaneDwellEntryAdjuster(mutableMapOf<String, Double>("retail" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 37203.9,
    )
}

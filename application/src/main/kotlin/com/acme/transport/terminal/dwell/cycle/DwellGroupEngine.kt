package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell group calculation. */
interface DwellGroupFactor {
    fun describe(): String
    fun adjust(item: DwellGroup, value: Double): Double
}

data class NameDwellGroupAdjuster(var factors: MutableMap<String, Double>) : DwellGroupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: DwellGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDwellGroupAdjuster(var step: Double) : DwellGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellGroup, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDwellGroupAdjuster(var cap: Double) : DwellGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellGroup, value: Double): Double = minOf(value, cap)
}

data class PercentDwellGroupAdjuster(var percent: Double) : DwellGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dwell group steps in order and keeps a trace of each. */
data class DwellGroupEngine(
    var steps: MutableList<DwellGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DwellGroup): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDwellGroupEngine(flags: Registry): DwellGroupEngine {
    return DwellGroupEngine(
        steps = mutableListOf<DwellGroupFactor>(
            NameDwellGroupAdjuster(mutableMapOf<String, Double>("retail" to 0.85)),
            RoundingDwellGroupAdjuster(1.0),
            FlaggedDwellGroupAdjuster(flags, "dwell-group-fallback", -8.6),
            CapDwellGroupAdjuster(22202.0),
            PercentDwellGroupAdjuster(-1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 52257.8,
    )
}

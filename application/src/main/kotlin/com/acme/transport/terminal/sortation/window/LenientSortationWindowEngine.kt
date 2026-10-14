package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient sortation window calculation. */
interface LenientSortationWindowStep {
    fun describe(): String
    fun adjust(item: LenientSortationWindow, value: Double): Double
}

data class FlaggedLenientSortationWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientSortationWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientSortationWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientSortationWindowAdjuster(var minimum: Double) : LenientSortationWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientSortationWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientSortationWindowAdjuster(var cap: Double) : LenientSortationWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientSortationWindow, value: Double): Double = minOf(value, cap)
}

data class LenientSortationWindowSequenceOffsetAdjuster(var weight: Double) : LenientSortationWindowStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: LenientSortationWindow, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class NameLenientSortationWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientSortationWindowStep {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientSortationWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient sortation window steps in order and keeps a trace of each. */
data class LenientSortationWindowEngine(
    var steps: MutableList<LenientSortationWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientSortationWindow): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientSortationWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientSortationWindowEngine(flags: Registry): LenientSortationWindowEngine {
    return LenientSortationWindowEngine(
        steps = mutableListOf<LenientSortationWindowStep>(
            FlaggedLenientSortationWindowAdjuster(flags, "lenient-sortation-window-rolling", 9.0),
            MinimumLenientSortationWindowAdjuster(34.5),
            CapLenientSortationWindowAdjuster(48345.0),
            LenientSortationWindowSequenceOffsetAdjuster(0.65),
            NameLenientSortationWindowAdjuster(mutableMapOf<String, Double>("north" to 0.91, "online" to 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 57255.8,
    )
}

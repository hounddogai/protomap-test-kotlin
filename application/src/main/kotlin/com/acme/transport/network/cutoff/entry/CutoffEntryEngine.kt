package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff entry calculation. */
interface CutoffEntryStep {
    fun describe(): String
    fun adjust(item: CutoffEntry, value: Double): Double
}

data class FlaggedCutoffEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCutoffEntryAdjuster(var cap: Double) : CutoffEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffEntry, value: Double): Double = minOf(value, cap)
}

data class NameCutoffEntryAdjuster(var factors: MutableMap<String, Double>) : CutoffEntryStep {
    override fun describe(): String = "by name"

    override fun adjust(item: CutoffEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCutoffEntryAdjuster(var step: Double) : CutoffEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the cutoff entry steps in order and keeps a trace of each. */
data class CutoffEntryEngine(
    var steps: MutableList<CutoffEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CutoffEntry): Double {
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

    fun evaluateAll(items: MutableList<CutoffEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCutoffEntryEngine(flags: Registry): CutoffEntryEngine {
    return CutoffEntryEngine(
        steps = mutableListOf<CutoffEntryStep>(
            FlaggedCutoffEntryAdjuster(flags, "cutoff-entry-tiered", 6.8),
            CapCutoffEntryAdjuster(18026.0),
            NameCutoffEntryAdjuster(mutableMapOf<String, Double>("west" to 0.87)),
            RoundingCutoffEntryAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 57552.4,
    )
}

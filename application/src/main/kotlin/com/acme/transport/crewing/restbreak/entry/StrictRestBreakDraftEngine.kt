package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict rest break draft calculation. */
interface StrictRestBreakDraftModifier {
    fun describe(): String
    fun adjust(item: StrictRestBreakDraft, value: Double): Double
}

data class RoundingStrictRestBreakDraftAdjuster(var step: Double) : StrictRestBreakDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRestBreakDraft, value: Double): Double = roundTo(value, step)
}

data class MinimumStrictRestBreakDraftAdjuster(var minimum: Double) : StrictRestBreakDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRestBreakDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictRestBreakDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRestBreakDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRestBreakDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapStrictRestBreakDraftAdjuster(var cap: Double) : StrictRestBreakDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRestBreakDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the strict rest break draft steps in order and keeps a trace of each. */
data class StrictRestBreakDraftEngine(
    var steps: MutableList<StrictRestBreakDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictRestBreakDraft): Double {
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

    fun evaluateAll(items: MutableList<StrictRestBreakDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictRestBreakDraftEngine(): StrictRestBreakDraftEngine {
    return StrictRestBreakDraftEngine(
        steps = mutableListOf<StrictRestBreakDraftModifier>(
            RoundingStrictRestBreakDraftAdjuster(0.25),
            MinimumStrictRestBreakDraftAdjuster(15.5),
            TieredStrictRestBreakDraftAdjuster(
                mutableListOf<Double>(3551.0, 5840.0),
                mutableListOf<Double>(1.17, 0.87),
            ),
            CapStrictRestBreakDraftAdjuster(38408.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 28171.2,
    )
}

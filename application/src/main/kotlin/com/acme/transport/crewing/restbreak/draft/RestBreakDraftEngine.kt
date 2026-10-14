package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break draft calculation. */
interface RestBreakDraftModifier {
    fun describe(): String
    fun adjust(item: RestBreakDraft, value: Double): Double
}

data class CapRestBreakDraftAdjuster(var cap: Double) : RestBreakDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakDraft, value: Double): Double = minOf(value, cap)
}

data class RestBreakDraftWeightOffsetAdjuster(var weight: Double) : RestBreakDraftModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RestBreakDraft, value: Double): Double = value + item.weight * weight
}

data class RoundingRestBreakDraftAdjuster(var step: Double) : RestBreakDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakDraft, value: Double): Double = roundTo(value, step)
}

data class PercentRestBreakDraftAdjuster(var percent: Double) : RestBreakDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rest break draft steps in order and keeps a trace of each. */
data class RestBreakDraftEngine(
    var steps: MutableList<RestBreakDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakDraft): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakDraftEngine(): RestBreakDraftEngine {
    return RestBreakDraftEngine(
        steps = mutableListOf<RestBreakDraftModifier>(
            CapRestBreakDraftAdjuster(18887.0),
            RestBreakDraftWeightOffsetAdjuster(1.35),
            RoundingRestBreakDraftAdjuster(5.0),
            PercentRestBreakDraftAdjuster(-8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 30728.8,
    )
}

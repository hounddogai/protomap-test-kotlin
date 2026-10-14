package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt draft calculation. */
interface AttemptDraftModifier {
    fun describe(): String
    fun adjust(item: AttemptDraft, value: Double): Double
}

data class CapAttemptDraftAdjuster(var cap: Double) : AttemptDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumAttemptDraftAdjuster(var minimum: Double) : AttemptDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotAttemptDraftAdjuster(var factors: MutableMap<String, Double>) : AttemptDraftModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: AttemptDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the attempt draft steps in order and keeps a trace of each. */
data class AttemptDraftEngine(
    var steps: MutableList<AttemptDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AttemptDraft): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAttemptDraftEngine(): AttemptDraftEngine {
    return AttemptDraftEngine(
        steps = mutableListOf<AttemptDraftModifier>(
            CapAttemptDraftAdjuster(41517.0),
            MinimumAttemptDraftAdjuster(47.6),
            DepotAttemptDraftAdjuster(mutableMapOf<String, Double>("online" to 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 52150.0,
    )
}

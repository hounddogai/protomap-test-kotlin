package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending tender score calculation. */
interface PendingTenderScoreStep {
    fun describe(): String
    fun adjust(item: PendingTenderScore, value: Double): Double
}

data class RoundingPendingTenderScoreAdjuster(var step: Double) : PendingTenderScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTenderScore, value: Double): Double = roundTo(value, step)
}

data class PercentPendingTenderScoreAdjuster(var percent: Double) : PendingTenderScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingTenderScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingTenderScoreAdjuster(var cap: Double) : PendingTenderScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTenderScore, value: Double): Double = minOf(value, cap)
}

/** Runs the pending tender score steps in order and keeps a trace of each. */
data class PendingTenderScoreEngine(
    var steps: MutableList<PendingTenderScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingTenderScore): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTenderScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingTenderScoreEngine(): PendingTenderScoreEngine {
    return PendingTenderScoreEngine(
        steps = mutableListOf<PendingTenderScoreStep>(
            RoundingPendingTenderScoreAdjuster(0.25),
            PercentPendingTenderScoreAdjuster(24.0),
            CapPendingTenderScoreAdjuster(8493.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 67310.2,
    )
}

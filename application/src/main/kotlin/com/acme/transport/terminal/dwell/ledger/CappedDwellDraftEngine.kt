package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped dwell draft calculation. */
interface CappedDwellDraftFactor {
    fun describe(): String
    fun adjust(item: CappedDwellDraft, value: Double): Double
}

data class CappedDwellDraftLimitOffsetAdjuster(var weight: Double) : CappedDwellDraftFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: CappedDwellDraft, value: Double): Double = value + item.limit.toDouble() * weight
}

data class MinimumCappedDwellDraftAdjuster(var minimum: Double) : CappedDwellDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDwellDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleCappedDwellDraftAdjuster(var factors: MutableMap<String, Double>) : CappedDwellDraftFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: CappedDwellDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedDwellDraftAdjuster(var step: Double) : CappedDwellDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDwellDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the capped dwell draft steps in order and keeps a trace of each. */
data class CappedDwellDraftEngine(
    var steps: MutableList<CappedDwellDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedDwellDraft): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDwellDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedDwellDraftEngine(): CappedDwellDraftEngine {
    return CappedDwellDraftEngine(
        steps = mutableListOf<CappedDwellDraftFactor>(
            CappedDwellDraftLimitOffsetAdjuster(0.05),
            MinimumCappedDwellDraftAdjuster(38.3),
            LocaleCappedDwellDraftAdjuster(mutableMapOf<String, Double>("east" to 1.35, "west" to 0.74)),
            RoundingCappedDwellDraftAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 3813.7,
    )
}

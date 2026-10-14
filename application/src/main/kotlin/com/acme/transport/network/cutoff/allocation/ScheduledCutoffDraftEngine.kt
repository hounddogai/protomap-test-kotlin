package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled cutoff draft calculation. */
interface ScheduledCutoffDraftStep {
    fun describe(): String
    fun adjust(item: ScheduledCutoffDraft, value: Double): Double
}

data class ScheduledCutoffDraftAttemptsOffsetAdjuster(var weight: Double) : ScheduledCutoffDraftStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ScheduledCutoffDraft, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class MinimumScheduledCutoffDraftAdjuster(var minimum: Double) : ScheduledCutoffDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledCutoffDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledCutoffDraftAdjuster(var cap: Double) : ScheduledCutoffDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledCutoffDraft, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledCutoffDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCutoffDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCutoffDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotScheduledCutoffDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledCutoffDraftStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ScheduledCutoffDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledCutoffDraftAdjuster(var step: Double) : ScheduledCutoffDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledCutoffDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled cutoff draft steps in order and keeps a trace of each. */
data class ScheduledCutoffDraftEngine(
    var steps: MutableList<ScheduledCutoffDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledCutoffDraft): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledCutoffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledCutoffDraftEngine(): ScheduledCutoffDraftEngine {
    return ScheduledCutoffDraftEngine(
        steps = mutableListOf<ScheduledCutoffDraftStep>(
            ScheduledCutoffDraftAttemptsOffsetAdjuster(0.92),
            MinimumScheduledCutoffDraftAdjuster(3.0),
            CapScheduledCutoffDraftAdjuster(8502.0),
            TieredScheduledCutoffDraftAdjuster(
                mutableListOf<Double>(3000.0, 3680.0),
                mutableListOf<Double>(1.16, 1.13),
            ),
            DepotScheduledCutoffDraftAdjuster(mutableMapOf<String, Double>("south" to 0.88)),
            RoundingScheduledCutoffDraftAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 42987.6,
    )
}

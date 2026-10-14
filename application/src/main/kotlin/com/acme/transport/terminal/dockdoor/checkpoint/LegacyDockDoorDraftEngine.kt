package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy dock door draft calculation. */
interface LegacyDockDoorDraftAdjuster {
    fun describe(): String
    fun adjust(item: LegacyDockDoorDraft, value: Double): Double
}

data class LegacyDockDoorDraftEndsAtOffsetAdjuster(var weight: Double) : LegacyDockDoorDraftAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LegacyDockDoorDraft, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class TitleLegacyDockDoorDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDockDoorDraftAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyDockDoorDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyDockDoorDraftAdjuster(var minimum: Double) : LegacyDockDoorDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDockDoorDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLegacyDockDoorDraftAdjuster(var cap: Double) : LegacyDockDoorDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyDockDoorDraft, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyDockDoorDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDockDoorDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDockDoorDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyDockDoorDraftAdjuster(var step: Double) : LegacyDockDoorDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDockDoorDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy dock door draft steps in order and keeps a trace of each. */
data class LegacyDockDoorDraftEngine(
    var steps: MutableList<LegacyDockDoorDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyDockDoorDraft): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyDockDoorDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyDockDoorDraftEngine(): LegacyDockDoorDraftEngine {
    return LegacyDockDoorDraftEngine(
        steps = mutableListOf<LegacyDockDoorDraftAdjuster>(
            LegacyDockDoorDraftEndsAtOffsetAdjuster(0.49),
            TitleLegacyDockDoorDraftAdjuster(mutableMapOf<String, Double>("central" to 0.9, "north" to 1.0)),
            MinimumLegacyDockDoorDraftAdjuster(36.5),
            CapLegacyDockDoorDraftAdjuster(36207.0),
            TieredLegacyDockDoorDraftAdjuster(
                mutableListOf<Double>(3742.0, 5142.0, 5635.0, 6714.0),
                mutableListOf<Double>(1.17, 1.1, 0.85, 0.94),
            ),
            RoundingLegacyDockDoorDraftAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 99560.8,
    )
}

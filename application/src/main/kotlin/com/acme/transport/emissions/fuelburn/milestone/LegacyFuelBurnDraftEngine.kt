package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy fuel burn draft calculation. */
interface LegacyFuelBurnDraftModifier {
    fun describe(): String
    fun adjust(item: LegacyFuelBurnDraft, value: Double): Double
}

data class LegacyFuelBurnDraftFloorValueOffsetAdjuster(var weight: Double) : LegacyFuelBurnDraftModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: LegacyFuelBurnDraft, value: Double): Double = value + item.floorValue * weight
}

data class ChannelLegacyFuelBurnDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyFuelBurnDraftModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: LegacyFuelBurnDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyFuelBurnDraftAdjuster(var cap: Double) : LegacyFuelBurnDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyFuelBurnDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyFuelBurnDraftAdjuster(var minimum: Double) : LegacyFuelBurnDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyFuelBurnDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyFuelBurnDraftAdjuster(var step: Double) : LegacyFuelBurnDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyFuelBurnDraft, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyFuelBurnDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyFuelBurnDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyFuelBurnDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy fuel burn draft steps in order and keeps a trace of each. */
data class LegacyFuelBurnDraftEngine(
    var steps: MutableList<LegacyFuelBurnDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyFuelBurnDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyFuelBurnDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyFuelBurnDraftEngine(): LegacyFuelBurnDraftEngine {
    return LegacyFuelBurnDraftEngine(
        steps = mutableListOf<LegacyFuelBurnDraftModifier>(
            LegacyFuelBurnDraftFloorValueOffsetAdjuster(1.57),
            ChannelLegacyFuelBurnDraftAdjuster(mutableMapOf<String, Double>("retail" to 1.04)),
            CapLegacyFuelBurnDraftAdjuster(7096.0),
            MinimumLegacyFuelBurnDraftAdjuster(49.0),
            RoundingLegacyFuelBurnDraftAdjuster(0.5),
            TieredLegacyFuelBurnDraftAdjuster(
                mutableListOf<Double>(3998.0, 4863.0),
                mutableListOf<Double>(1.15, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 8752.9,
    )
}

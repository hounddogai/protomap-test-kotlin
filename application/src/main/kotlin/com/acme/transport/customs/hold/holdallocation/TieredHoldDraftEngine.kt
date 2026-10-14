package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered hold draft calculation. */
interface TieredHoldDraftModifier {
    fun describe(): String
    fun adjust(item: TieredHoldDraft, value: Double): Double
}

data class CapTieredHoldDraftAdjuster(var cap: Double) : TieredHoldDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredHoldDraft, value: Double): Double = minOf(value, cap)
}

data class TieredTieredHoldDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredHoldDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredHoldDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredHoldDraftAdjuster(var step: Double) : TieredHoldDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredHoldDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered hold draft steps in order and keeps a trace of each. */
data class TieredHoldDraftEngine(
    var steps: MutableList<TieredHoldDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredHoldDraft): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredHoldDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredHoldDraftEngine(): TieredHoldDraftEngine {
    return TieredHoldDraftEngine(
        steps = mutableListOf<TieredHoldDraftModifier>(
            CapTieredHoldDraftAdjuster(4162.0),
            TieredTieredHoldDraftAdjuster(
                mutableListOf<Double>(3950.0, 6094.0, 6724.0, 7330.0),
                mutableListOf<Double>(0.89, 1.02, 0.8, 0.83),
            ),
            RoundingTieredHoldDraftAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 43959.3,
    )
}

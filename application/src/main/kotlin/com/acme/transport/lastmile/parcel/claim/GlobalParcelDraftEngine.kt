package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global parcel draft calculation. */
interface GlobalParcelDraftStep {
    fun describe(): String
    fun adjust(item: GlobalParcelDraft, value: Double): Double
}

data class NameGlobalParcelDraftAdjuster(var factors: MutableMap<String, Double>) : GlobalParcelDraftStep {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalParcelDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalParcelDraftAdjuster(var cap: Double) : GlobalParcelDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalParcelDraft, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalParcelDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalParcelDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalParcelDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalParcelDraftExpiresAtOffsetAdjuster(var weight: Double) : GlobalParcelDraftStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: GlobalParcelDraft, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingGlobalParcelDraftAdjuster(var step: Double) : GlobalParcelDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalParcelDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the global parcel draft steps in order and keeps a trace of each. */
data class GlobalParcelDraftEngine(
    var steps: MutableList<GlobalParcelDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalParcelDraft): Double {
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

    fun evaluateAll(items: MutableList<GlobalParcelDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalParcelDraftEngine(): GlobalParcelDraftEngine {
    return GlobalParcelDraftEngine(
        steps = mutableListOf<GlobalParcelDraftStep>(
            NameGlobalParcelDraftAdjuster(
                mutableMapOf<String, Double>("central" to 1.35, "east" to 1.23, "retail" to 0.86),
            ),
            CapGlobalParcelDraftAdjuster(23577.0),
            TieredGlobalParcelDraftAdjuster(
                mutableListOf<Double>(1792.0, 2825.0),
                mutableListOf<Double>(1.06, 0.85),
            ),
            GlobalParcelDraftExpiresAtOffsetAdjuster(0.79),
            RoundingGlobalParcelDraftAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 29260.5,
    )
}
